#!/bin/bash

# SSH key check
if [ -z "$SSH_AUTH_SOCK" ]; then
    eval $(ssh-agent -s)
    ssh-add ~/.ssh/id_ed25519
fi


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
if [ ! -d "resources/public" ]; then
    echo "Error: resources/public directory not found"
    exit 1
fi
# <<<<<
# ======================================================================

# Creating clean copy to restore later

cp resources/public/index.html resources/public/index.html.bak

get_last_version() {
    local found_version=""
    if [ -f CHANGELOG.md ]; then
        found_version=$(head -n 1 CHANGELOG.md | grep -o "^v[0-9]*\.[0-9]*\.[0-9]* [a-f0-9]\{40\}" | cut -d' ' -f1 || echo "")
    fi

    if [ -z "$found_version" ]; then
        git_version=$(git describe --tags --abbrev=0 2>/dev/null || echo "")
        if [ ! -z "$git_version" ]; then
            found_version=$git_version
        fi
    fi

    if [ -z "$found_version" ]; then
        js_version=$(ls resources/public/js/main.v*.js 2>/dev/null | sed 's/.*main\.\(v[0-9]*\.[0-9]*\.[0-9]*\)\.js/\1/' | sort -V | tail -n 1)
        if [ ! -z "$js_version" ]; then
            found_version=$js_version
        fi
    fi

    if [ -z "$found_version" ]; then
        found_version="v0.0.1"
    fi

    printf "%s\n" "$found_version"
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
    if ! [[ "$major" =~ ^[0-9]+$ ]] || ! [[ "$minor" =~ ^[0-9]+$ ]] || ! [[ "$patch" =~ ^[0-9]+$ ]]; then
        echo "Invalid version numbers: $major.$minor.$patch" >&2
        return 1
    fi

    if [ "$upgrade_minor" = true ]; then
        minor=$((minor + 1))
        patch=0
    else
        patch=$((patch + 1))
    fi
    echo "$major.$minor.$patch"
}

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

    CSS_VERSIONED_STYLE="css/style.$version_with_v.css"
    CSS_VERSIONED_TAILWIND="tailwind.$version_with_v.css"
    cp resources/public/css/style.css resources/public/"$CSS_VERSIONED_STYLE"
    cp resources/public/tailwind.css resources/public/"$CSS_VERSIONED_TAILWIND"

    NEW_MAIN="main.$version_with_v.js"
    
    # git commits since last version
    current_commit=$(git rev-parse HEAD)
    last_commit=$(head -n 1 CHANGELOG.md | grep -o "[a-f0-9]\{40\}" || echo "")

    # Update CHANGELOG.md
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

    sed -i -E "s|<script src=\"js/main.js\"></script>|<script src=\"js/$NEW_MAIN\"></script>|" resources/public/index.html
    sed -i -E "s|<link href=\"css/style.css\" rel=\"stylesheet\" type=\"text/css\">|<link href=\"/$CSS_VERSIONED_STYLE\"  rel=\"stylesheet\" type=\"text/css\">|" resources/public/index.html
    sed -i -E "s|<link href=\"tailwind.css\" rel=\"stylesheet\" type=\"text/css\">|<link href=\"/$CSS_VERSIONED_TAILWIND\"  rel=\"stylesheet\" type=\"text/css\">|" resources/public/index.html

    # Build normally first
    echo "Building application..."
    npx shadow-cljs release app
    if [ $? -ne 0 ]; then
        echo "Error: Shadow-cljs compilation failed"
        rollback
        exit 1
    fi

    # Rename to versioned file
    echo "Creating versioned file: main.$version_with_v.js"
    cp resources/public/js/main.js "resources/public/js/main.$version_with_v.js"
    if [ $? -ne 0 ]; then
        echo "Error: Failed to create versioned JS file"
        rollback
        exit 1
    fi

    # Update manifest
    echo "{\"main.js\": \"js/main.$version_with_v.js\"}" > resources/public/js/manifest.edn
fi


rollback() {
    echo "Deployment failed. Rolling back changes..."
    rm -f resources/public/css/style.v*.css
    rm -f resources/public/tailwind.v*.css
    rm -f resources/public/js/main.v*.js
    mv resources/public/index.html.bak resources/public/index.html
    echo "Rollback complete. No commit was created."
}

rsync -avz --progress \
      --exclude "js/cljs-runtime" \
      --exclude "js/main.js" \
      --exclude "portfolio.html" \
      --exclude "portfolio-js" \
      --exclude "*.edn" \
      --exclude "*.bak" \
      resources/public/ \
      nando@157.90.230.213:/home/nando/apps/qoback/fik

if [ $? -ne 0 ]; then
    rollback
    exit 1
fi

# Commit build artifacts and version updates
git add CHANGELOG.md resources/public/index.html resources/public/css/ resources/public/tailwind.v*.css
git commit -m "Release $version_with_v"

# Push to remote
git push


clean() {
    # Clean old versions
    rm resources/public/*.bak
    rm -rf resources/public/portfolio-js
    rm resources/public/js/main.v*.js
    rm resources/public/js/manifest.edn
    rm resources/public/css/style.v*.css
    rm resources/public/tailwind.v*.css
}


# Clean old versions if requested
if [[ $clean ]]; then
    echo "Cleaning old version files..."
    clean
fi

