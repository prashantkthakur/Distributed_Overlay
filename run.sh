current_path=$(pwd)
out_home="$current_path/out/production/PA"
build_home="$current_path/build/classes/java/main/"
REG_HOSTNAME="acorn.cs.colostate.edu"
PORT='5555'

#if [ -d $out_home ];then
#	echo "Registry.."
#	dbus-launch gnome-terminal -x bash -c "ssh -t ${REG_HOSTNAME} 'cd ${out_home};java cs455.overlay.node.Registry $PORT';bash;"
#fi
#sleep 2

for i in `cat machine_list`; do
echo 'logging into '${i}
if [ -d $out_home ];then
	echo "Login for out"
	dbus-launch gnome-terminal -x bash -c "ssh -t ${i} 'cd ${out_home}; java cs455.overlay.node.MessagingNode $REG_HOSTNAME $PORT;bash;'" &
else
	echo "Build"
	dbus-launch gnome-terminal -x bash -c "ssh -t ${i} 'cd ${build_home}; java cs455.overlay.node.MessagingNode $REG_HOSTNAME $PORT;bash;'" &
fi
sleep 1
done

