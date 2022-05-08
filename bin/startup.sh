#!/usr/bin/env zsh
# shellcheck disable=SC2164
# 详细参数解释请看站长.
cd ..
rm -rf ./bootstrap.jar
jar cvf0 bootstrap.jar -C out/production/jerrymouse cn/zhaobin/jerrymouse/Bootstrap.class -C out/production/jerrymouse cn/zhaobin/jerrymouse/classloader/CommonClassLoader.class
# 删除 lib 目录下的jerry-mouse.jar
rm -rf ./lib/jerry-mouse.jar
cd out
cd production
cd jerrymouse
jar cvf0 ../../../lib/jerry-mouse.jar *
# shellcheck disable=SC2103
cd ..
cd ..
cd ..
java -cp bootstrap.jar cn/zhaobin/jerrymouse/Bootstrap
