#!/bin/bash
function report_failed_deploy() {
	echo "> Deploy failed"
	exit 1
}

if [ $# -gt 1 ]
  then
    echo "> Invalid arguments supplied"
else
	# Make sure we're on the right branch and at HEAD
    cd ~/src/familyhub
	git checkout master
	if [ $? -ne 0 ]
    	then
    		report_failed_deploy
	fi
	git fetch github
	git rebase github/master
	if [ $? -ne 0 ]
    	then 
	    	report_failed_deploy
	fi

	#Generate new version code by incrementing last one
    old_v=`cat version_code`
    let "new_v=old_v+1"

    #Tag release
    if [ $# -eq 1 ]
    	then
    	    tag_name=v$new_v-$1
    else
    	tag_name=v$new_v
	fi
    echo "> Creating new release $tag_name (previous was $old_v)"
	git tag $tag_name
    #git push github $tag_name
    if [ $? -ne 0 ]
    	then 
	    	report_failed_deploy
	else
		echo $new_v >> version_code
		git add version_code
		git commit -m "Bumping version to $new_v"
		#git push github master

		if [ $? -eq 0 ]
    		then
    			echo "> $tag_name pushed and version updated in repo"
		fi
	fi
fi
