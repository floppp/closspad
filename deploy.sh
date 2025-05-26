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


get_last_version() {
    local found_version=""
    printf "Starting version detection...\n" >&2
    if [ -f CHANGELOG.md ]; then
        printf "Found CHANGELOG.md\n" >&2
        printf "First line of CHANGELOG.md:\n\t" >&2
        head -n 1 CHANGELOG.md >&2
        found_version=$(head -n 1 CHANGELOG.md | grep -o "^v[0-9]*\.[0-9]*\.[0-9]* [a-f0-9]\{40\}" | cut -d' ' -f1 || echo "")
        if [ ! -z "$found_version" ]; then
            printf "Found version in CHANGELOG.md: %s\n" "$found_version" >&2
        else
            printf "No version found in first line of CHANGELOG.md\n" >&2
        fi
    else
        printf "CHANGELOG.md not found\n" >&2
    fi

    if [ -z "$found_version" ]; then
        printf "Checking git tags...\n" >&2
        git_version=$(git describe --tags --abbrev=0 2>/dev/null || echo "")
        if [ ! -z "$git_version" ]; then
            printf "Found version in git tags: %s\n" "$git_version" >&2
            found_version=$git_version
        else
            printf "No git tags found\n" >&2
        fi
    fi

    if [ -z "$found_version" ]; then
        printf "Checking JS files...\n" >&2
        # ls -la resources/public/js/main.v*.js 2>/dev/null >&2
        js_version=$(ls resources/public/js/main.v*.js 2>/dev/null | sed 's/.*main\.\(v[0-9]*\.[0-9]*\.[0-9]*\)\.js/\1/' | sort -V | tail -n 1)
        if [ ! -z "$js_version" ]; then
            printf "Found version in JS files: %s\n" "$js_version" >&2
            found_version=$js_version
        else
            printf "No versioned JS files found\n" >&2
        fi
    fi

    if [ -z "$found_version" ]; then
        found_version="v0.0.1"
        printf "No version found anywhere, using default: %s\n" "$found_version" >&2
    fi

    printf "Final version selected: %s\n" "$found_version" >&2
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
        *)
            other_args+=("$arg")
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
        # If version was provided via -v flag, add 'v' prefix if not present
        version_with_v=${version#v}
        version_with_v="v$version_with_v"
    fi

    NEW_MAIN="main.$version_with_v.js"
    printf "NEW_MAIN value: %s\n" "$NEW_MAIN" >&2

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

    printf "Checking index.html before update:\n" >&2
    grep "<script src=\"js/main" resources/public/index.html >&2

    sed -i.bak -E "s|<script src=\"/js/main.js\"></script>|<script src=\"js/$NEW_MAIN\"></script>|" resources/public/index.html

    printf "Checking index.html after update:\n" >&2
    grep "<script src=\"js/main" resources/public/index.html >&2

    # Clean old versions
    rm -f resources/public/js/main.v*.js
    rm -f resources/public/js/manifest.edn

    # Build normally first
    echo "Building application..."
    npx shadow-cljs release app
    if [ $? -ne 0 ]; then
        echo "Error: Shadow-cljs compilation failed"
        exit 1
    fi

    # Rename to versioned file
    echo "Creating versioned file: main.$version_with_v.js"
    mv resources/public/js/main.js "resources/public/js/main.$version_with_v.js"
    if [ $? -ne 0 ]; then
        echo "Error: Failed to create versioned JS file"
        exit 1
    fi

    # Verify new version
    echo "Build artifacts:"
    ls -la resources/public/js/

    # Update manifest
    echo "{\"main.js\": \"js/main.$version_with_v.js\"}" > resources/public/js/manifest.edn
fi



# Add trap to clean up on script exit in case there is some issue on syncing.
trap 'mv resources/public/index.html.bak resources/public/index.html' EXIT

rsync -avz --progress \
      --exclude "js/cljs-runtime" \
      --exclude "js/main.js" \
      --exclude "*.edn"\
      resources/public/ \
      nando@157.90.230.213:/home/nando/apps/qoback/fik

# Altough trap we must `mv` to avoid git issues.
mv resources/public/index.html.bak resources/public/index.html

# To add CHANGELOG change to last commit.
git add .
git commit --amend --no-edit
