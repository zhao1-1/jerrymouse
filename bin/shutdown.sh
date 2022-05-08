#!/usr/bin/env zsh
# 结束由 startup.sh 启动的tomcat进程
# shellcheck disable=SC2006
# shellcheck disable=SC2009
echo "正在关闭JerryMouse(╮（╯▽╰）╭ )"
# 查找启动进程PID号
JerryMouse_Id=$(ps -ef | grep java | grep -v "grep" | awk '{print $2}')
# 判断$JerryMouse_Id不为空
if [ "$JerryMouse_Id" ]; then
  for id in $JerryMouse_Id; do
    # 结束进程
    kill -9 "$id"
    echo "有关JerryMouse_Id进程${id}已经结束了, 如果有缘, 期待我们下一次再见~~"
  done
  echo "JerryMouse 关闭完成（＞﹏＜）"

else
  echo "JerryMouse 进程不存在.请稍后尝试.(（￣３￣）a)"
fi