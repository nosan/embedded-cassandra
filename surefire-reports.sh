#!/bin/bash
echo
echo "===== SUREFIRE REPORTS ====="
echo
for dir in $(find . -type d -name 'surefire-reports'); do
  for file in $(find "$dir" -type f -name '*.txt'); do
     if grep -E "(Failures|Errors): [^0]" ${file}; then
        cat ${file}
        echo
        if  [[ -f "${file%.*}-output.txt" ]]; then
           cat "${file%.*}-output.txt"
        fi
        echo
        echo
     fi
  done
done

