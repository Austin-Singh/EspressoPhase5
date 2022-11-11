#!/bin/bash

RED=$(tput setaf 1)
GREEN=$(tput setaf 2)
NORMAL=$(tput sgr0)

function run_tests {

        goodResults=0
        badResults=0
        total=0

        for f in $1
        do
                ((total=total+1))
                ./espressoc $f > me.txt 2> /dev/null
                ./espressocr $f > ref.txt 2> /dev/null

                diff -u me.txt ref.txt > diff.txt
                if [ -s diff.txt ]; then

                        LINE1=$(tail -1 me.txt | sed 's/.*: //')
                        LINE2=$(tail -1 ref.txt | sed 's/.*: //')

                        if [ "$LINE1" = "$LINE2" ]; then
                                ((goodResults=goodResults+1))
                                printf "%-15s %-110s %-15s\n\n" "Processing ..." $f "${GREEN}ERROR MESSAGE MATCHING${NORMAL} [$total]"
                        else
                                ((badResults=badResults+1))
                                printf "%-15s %-110s %-15s\n" "Processing ..." $f "${RED}NOT MATCHING${NORMAL} [$total]"
                                printf "\t$LINE1\n"
                                printf "\t$LINE2\n\n"                        
                        fi

                else 
                        ((goodResults=goodResults+1))
                        printf "%-15s %-110s %-15s\n\n" "Processing ..." $f "${GREEN}MATCHING${NORMAL} [$total]"
                fi
        done

        printf "Passed Tests: ${GREEN}$goodResults${NORMAL} of $total passed (${RED}$badResults${NORMAL} failed)\n\n"
}

run_tests "/home/wsl/EspressoPhase4/Tests/Phase4/Espresso/GoodTests/*"
run_tests "/home/wsl/EspressoPhase4/Tests/Phase4/Espresso/BadTests/*"

