#! /usr/bin/python

import json
from subprocess import Popen, PIPE, STDOUT
import sys
import uuid

if (len(sys.argv) < 2):
	sys.exit('No command given!')

if sys.argv[1] == 'announce':
	jsonstr = json.dumps({
		'game' : {
			'game' : {
				'id' : str(uuid.uuid4()),
				'name' : 'test game'
			},
			'map' : 'map',
			'server' : {
				'id' : str(uuid.uuid4()),
				'name' : 'test server'
			},
			'state' : 'ANNOUNCED'
		}
	})
else:
	sys.exit('Unknown command!')

p = Popen(['dtnsend', '--src', 'server', '--lifetime', '100', '-g', "dtn://beavergame.dtn/client"], stdout=PIPE, stdin=PIPE, stderr=STDOUT)

stdout, stderr = p.communicate(input=jsonstr)

print(stdout)
if stderr:
	print(stderr)

