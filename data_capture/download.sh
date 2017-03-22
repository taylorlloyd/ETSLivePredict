NUM=0
while [ $NUM -lt 120 ]
do
    curl -s -L "https://data.edmonton.ca/download/7qed-k2fc/application%2Foctet-stream" -o ${NUM}.gtfs
    echo "Downloaded ${NUM}.gtfs"
    NUM=$(($NUM + 1))
    sleep 30
done
