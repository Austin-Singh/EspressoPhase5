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
                        grep -E '^(\+\/|\-\/)' diff.txt > diff2.txt
                        cat diff2.txt | sed 's/.*: //' > diff3.txt

                        #while read line; do echo $line; done < diff3.txt

                        LINE1=$(head -1 diff3.txt)
                        LINE2=$(tail -1 diff3.txt)

                        if [ "$LINE1" = "$LINE2" ]; then
                                ((goodResults=goodResults+1))
                                printf "%-15s %-110s %-15s\n\n" "Processing ..." $f "${GREEN}MATCHING${NORMAL} [$total]"
                        else
                                printf "%-15s %-110s %-15s\n" "Processing ..." $f "${RED}NOT MATCHING${NORMAL} [$total]"
                                ((badResults=badResults+1))
                                printf "\t%s $LINE1\n"
                                printf "\t%s $LINE2\n\n"
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

