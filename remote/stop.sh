#!/bin/bash
cd "$(dirname "$0")"
[ -f app.pid ] && kill $(cat app.pid) 2>/dev/null; rm -f app.pid

