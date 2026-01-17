#!/bin/bash

# SSH key check
if [ -z "$SSH_AUTH_SOCK" ]; then
    eval $(ssh-agent -s)
    ssh-add ~/.ssh/id_ed25519
    fi

# ======================================================================
# Configuration
# ======================================================================
PUBLIC_DIR="resources/public"

# ======================================================================
# Functions
# ======================================================================

rollback() {
    echo "Deployment failed. Rolling back changes..."
    rm -f $PUBLIC_DIR/css/style.v*.css
    rm -f $PUBLIC_DIR/tailwind.v*.css
    rm -f $PUBLIC_DIR/js/main.v*.js
    mv $PUBLIC_DIR/index.html.bak $PUBLIC_DIR/index.html
    echo "Rollback complete. No commit was created."
}

clean() {
    # Clean old versions
    rm $PUBLIC_DIR/*.bak
    rm -rf $PUBLIC_DIR/portfolio-js
    rm $PUBLIC_DIR/js/main.v*.js
    rm $PUBLIC_DIR/js/manifest.edn
    rm $PUBLIC_DIR/css/style.v*.css
    rm $PUBLIC_DIR/tailwind.v*.css
}

get_last_version() {
    # Try CHANGELOG.md first
    if [ -f CHANGELOG.md ]; then
        local version=$(head -n 1 CHANGELOG.md | grep -o "^v[0-9]*\.[0-9]*\.[0-9]* [a-f0-9]\{40\}" | cut -d' ' -f1 || echo "")
        [ ! -z "$version" ] && echo "$version" && return 0
    fi

    # Try git tags
    local version=$(git describe --tags --abbrev=0 2>/dev/null || echo "")
    [ ! -z "$version" ] && echo "$version" && return 0

    # Try JS files
    local version=$(ls $PUBLIC_DIR/js/main.v*.js 2>/dev/null | sed 's/.*main\.\(v[0-9]*\.[0-9]*\.[0-9]*\)\.js/\1/' | sort -V | tail -n 1)
    [ ! -z "$version" ] && echo "$version" && return 0

    # Default
    echo "v0.0.1"
}

increment_version() {
    local input_version=$1
    local upgrade_minor=$2
    # Strip any leading 'v' if present
    local version=${input_version#v}
    if [[ ! $version =~ ^[0-9]+\.[0-9]+\.[0-9]+$ ]]; then
        echo "Invalid version format: $input_version" >&2
        return 1
    fi

    local major minor patch
    IFS='.' read -r major minor patch <<< "$version"

    if [ "$upgrade_minor" = true ]; then
        minor=$((minor + 1))
        patch=0
    else
        patch=$((patch + 1))
    fi
    echo "$major.$minor.$patch"
}

compile_release() {
    # Determine version
    if [[ -z "$version" ]]; then
        last_version=$(get_last_version | tail -n 1)
        version=$(increment_version "$last_version" "$minor_upgrade")
        version_with_v="v$version"
        if [ "$minor_upgrade" = true ]; then
            echo "Auto-incrementing minor version to: $version_with_v"
        else
            echo "Auto-incrementing patch version to: $version_with_v"
        fi
    else
        version_with_v="v${version#v}"
    fi

    # Create versioned CSS files
    CSS_VERSIONED_STYLE="css/style.$version_with_v.css"
    CSS_VERSIONED_TAILWIND="tailwind.$version_with_v.css"
    cp $PUBLIC_DIR/css/style.css "$PUBLIC_DIR/$CSS_VERSIONED_STYLE"
    cp $PUBLIC_DIR/tailwind.css "$PUBLIC_DIR/$CSS_VERSIONED_TAILWIND"

    NEW_MAIN="main.$version_with_v.js"

    # Update CHANGELOG.md
    current_commit=$(git rev-parse HEAD)
    last_commit=$(head -n 1 CHANGELOG.md | grep -o "[a-f0-9]\{40\}" || echo "")

    printf "%s %s\n" "$version_with_v" "$current_commit" > CHANGELOG.md.tmp
    if [ ! -z "$last_commit" ]; then
        commit_log=$(git log --pretty=format:"    >> %s" ${last_commit}..HEAD)
        if [ ! -z "$commit_log" ]; then
            echo "$commit_log" >> CHANGELOG.md.tmp
            echo "" >> CHANGELOG.md.tmp
        fi
    fi
    echo "" >> CHANGELOG.md.tmp
    if [ -f CHANGELOG.md ]; then
        cat CHANGELOG.md >> CHANGELOG.md.tmp
    fi
    mv CHANGELOG.md.tmp CHANGELOG.md

    # Update index.html with versioned assets
    sed -i -E \
      -e "s|<script src=\"js/main.js\"></script>|<script src=\"js/$NEW_MAIN\"></script>|" \
      -e "s|<link href=\"css/style.css\"  rel=\"stylesheet\" type=\"text/css\">|<link href=\"$CSS_VERSIONED_STYLE\"  rel=\"stylesheet\" type=\"text/css\">|" \
      -e "s|<link href=\"tailwind.css\"  rel=\"stylesheet\" type=\"text/css\">|<link href=\"$CSS_VERSIONED_TAILWIND\"  rel=\"stylesheet\" type=\"text/css\">|" \
      $PUBLIC_DIR/index.html

    # Build application
    echo "Building application..."
    npx shadow-cljs release app
    if [ $? -ne 0 ]; then
        echo "Error: Shadow-cljs compilation failed"
        rollback
        exit 1
    fi

    # Create versioned JS file
    echo "Creating versioned file: main.$version_with_v.js"
    cp $PUBLIC_DIR/js/main.js "$PUBLIC_DIR/js/main.$version_with_v.js"
    if [ $? -ne 0 ]; then
        echo "Error: Failed to create versioned JS file"
        rollback
        exit 1
    fi

    # Update manifest
    echo "{\"main.js\": \"js/main.$version_with_v.js\"}" > $PUBLIC_DIR/js/manifest.edn
}

# ======================================================================
# >>>>> Guards
git fetch origin

# Git no dirty branch
if [[ -n $(git status -s) ]]; then
    printf "Error: Git working directory is not clean.\nPlease commit or stash your changes before deploying.\n" >&2
    git status
    exit 1
fi

# Remote changes guard
CURRENT_BRANCH=$(git rev-parse --abbrev-ref HEAD)
TRACKING_BRANCH=$(git rev-parse --abbrev-ref --symbolic-full-name @{u} 2>/dev/null)
if [ $? -ne 0 ]; then
    printf "Error: Current branch '%s' is not tracking a remote branch.\n" "$CURRENT_BRANCH" >&2
    exit 1
fi
if [[ -n $(git log HEAD..$TRACKING_BRANCH) ]]; then
    printf "Error: There are changes in the remote branch that haven't been pulled.\nPlease pull the latest changes before deploying.\n" >&2
    git log HEAD..$TRACKING_BRANCH --oneline
    exit 1
fi

# Folder guard
if [ ! -d "$PUBLIC_DIR" ]; then
    echo "Error: $PUBLIC_DIR directory not found"
    exit 1
fi
# <<<<<
# ======================================================================

# Creating clean copy to restore later

cp $PUBLIC_DIR/index.html $PUBLIC_DIR/index.html.bak

minor_upgrade=false
version=""

# `major` upgrade must be done manually, e.g. `-v 1.0.0`.
for arg in "$@"; do
    case $arg in
        -c)
            compile=true
            shift 1
            ;;
        -v)
            if [ "$minor_upgrade" = true ]; then
                echo "Error: Cannot use -v with -m flag" >&2
                exit 1
            fi
            version=$2
            shift 2
            ;;
        -m)
            if [ ! -z "$version" ]; then
                echo "Error: Cannot use -m with -v flag" >&2
                exit 1
            fi
            minor_upgrade=true
            shift 1
            ;;
        --clean)
            clean=true
            shift 1
            ;;
    esac
done

if [[ $compile ]]; then
    compile_release
fi


if ! rsync -avz --progress \
     --exclude 'js/cljs-runtime' \
     --exclude 'js/main.js' \
     --exclude 'portfolio.html' \
     --exclude 'portfolio-js' \
     --exclude '*.edn' \
     --exclude '*.bak' "$PUBLIC_DIR/" \
     nando@157.90.230.213:/home/nando/apps/qoback/fik; then
    rollback
    exit 1
fi

mv $PUBLIC_DIR/index.html.bak $PUBLIC_DIR/index.html

# Commit build artifacts and version updates
git add CHANGELOG.md $PUBLIC_DIR/index.html $PUBLIC_DIR/css/ $PUBLIC_DIR/tailwind.v*.css
git commit -m "Release $version_with_v"

# Push to remote
if ! git push; then
    echo "Error: Git push failed"
    exit 1
fi


# Clean old versions if requested
if [[ $clean ]]; then
    echo "Cleaning old version files..."
    clean
fi
