#!/bin/bash

RED=$(tput setaf 1)
GREEN=$(tput setaf 2)
NORMAL=$(tput sgr0)

function run_Goodtests {

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
                        ((badResults=badResults+1))
                        printf "%-15s %-110s %-15s\n\n" "Processing ..." $f "${RED}OUTPUT NOT MATCHING${NORMAL} [$total]"
                        cat diff.txt # show difference in the outputs
                else 
                        ((goodResults=goodResults+1))
                        printf "%-15s %-110s %-15s\n\n" "Processing ..." $f "${GREEN}OUTPUT MATCHING${NORMAL} [$total]"
                fi
        done

        printf "Passed Tests: ${GREEN}$goodResults${NORMAL} of $total passed (${RED}$badResults${NORMAL} failed)\n\n"
}

function run_Badtests {

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
                                printf "%-15s %-110s %-15s\n" "Processing ..." $f "${GREEN}ERROR MESSAGE MATCHING${NORMAL} [$total]"
                                printf "\tOUR: $LINE1\n"
                                printf "\tREF: $LINE2\n\n"     
                        else
                                ((badResults=badResults+1))
                                printf "%-15s %-110s %-15s\n" "Processing ..." $f "${RED}ERROR MESSAGE NOT MATCHING${NORMAL} [$total]"
                                printf "\tOUR: $LINE1\n"
                                printf "\tREF: $LINE2\n\n"     
                        fi

                else 
                        ((goodResults=goodResults+1))
                        printf "%-15s %-110s %-15s\n\n" "Processing ..." $f "${GREEN}MATCHING${NORMAL} [$total]"
                fi
        done

        printf "Passed Tests: ${GREEN}$goodResults${NORMAL} of $total passed (${RED}$badResults${NORMAL} failed)\n\n"
}

run_Goodtests "/home/wsl/EspressoPhase5/Tests/Phase5/Espresso/GoodTests/*"
run_Badtests "/home/wsl/EspressoPhase5/Tests/Phase5/Espresso/BadTests/*"