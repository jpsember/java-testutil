#!/usr/bin/env bash
set -eu

dfa input tokens.rxp \
    output "_SKIP_tokens.dfa" \
    ids "src/test/java/js/parsing/Tokens.java"
