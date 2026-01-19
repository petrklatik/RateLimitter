#!/bin/bash

if [[ "$1" == "--help" || "$1" == "-h" ]]; then
  echo "Usage: ./test.sh <count> <client-id>"
  echo ""
  echo "Arguments:"
  echo "  count      Number of requests to send (default: 100)"
  echo "  client-id  Client identifier for X-Client-Id header (default: test-client)"
  echo ""
  echo "Example:"
  echo "  ./test.sh 150 client1"
  exit 0
fi

COUNT=${1:-100}
CLIENT_ID=${2:-test-client}

echo "Sending $COUNT requests for client '$CLIENT_ID'..."
seq 1 $COUNT | xargs -P 50 -I {} curl -s -o /dev/null -w "%{http_code}\n" \
  -H "X-Client-Id: $CLIENT_ID" http://localhost:8080/api/test | sort | uniq -c
