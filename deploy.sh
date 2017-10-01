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

    #Tag release
	current_v=`cat version_code`
    if [ $# -eq 1 ]
    	then
    	    tag_name=v$current_v-$1
    else
    	tag_name=v$current_v
	fi
    echo "> Creating new release $tag_name"
	git tag $tag_name
	if [ $? -ne 0 ]
    	then 
	    	report_failed_deploy
	fi

    git push github $tag_name
    if [ $? -ne 0 ]
    	then 
	    	report_failed_deploy
	else
		#Generate new version code by incrementing last one
	    let "new_v=current_v+1"

		echo $new_v > version_code
		git add version_code
		git commit -m "Bumping version to $new_v"
		git push github master

		if [ $? -eq 0 ]
    		then
    			echo "> $tag_name and version update pushed to remote repo"
		fi
	fi
fi
