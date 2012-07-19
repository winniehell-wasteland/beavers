#! /usr/bin/python

import json
import os
from pprint import pprint
from subprocess import Popen, PIPE, STDOUT
import sys
import uuid

if (len(sys.argv) != 3):
	sys.exit('usage: ' + sys.argv[0] + ' TARGET COMMAND')

if not sys.argv[1] in ['client', 'server']:
	sys.exit('TARGET must be "client" or "server"!')

if not os.path.exists(sys.argv[2]):
	sys.exit('File "' + sys.argv[2] + '" does not exist!')

try:
	f = open(sys.argv[2])
	command = json.load(f)
	f.close()
except Exception, e:
	sys.exit(unicode(e))

try:
	f = open('settings.json')
	settings = json.load(f)
	f.close()
except IOError:
	settings = {
		u'server' : unicode(uuid.uuid4()),
		u'client' : unicode(uuid.uuid4()),
		u'game' : unicode(uuid.uuid4())
	}
	
	f = open('settings.json', 'w')
	json.dump(settings, f)
	f.close()

def replace_placeholders(command):
	global settings
	
	if isinstance(command, int) or isinstance(command, float):
		return command
	elif isinstance(command, unicode):
		for placeholder in settings:
			command = command.replace('$' + placeholder.upper(), settings[placeholder])
		return command
	elif isinstance(command, dict):
		for key in command:
			command[key] = replace_placeholders(command[key])
		return command
	elif isinstance(command, list):
		return [replace_placeholders(entry) for entry in command]
	else:
		raise Exception('unknown class: ' + unicode(type(command)))

command = json.dumps(replace_placeholders(command))

#pprint(settings)
pprint(command)

print 'Sending to dtn://battlebeavers.dtn/'+sys.argv[1]
p = Popen(['dtnsend', '--src', 'server', '--lifetime', '300', '-g', 'dtn://battlebeavers.dtn/'+sys.argv[1]], stdout=PIPE, stdin=PIPE, stderr=STDOUT)

stdout, stderr = p.communicate(input=command)

print(stdout)
if stderr:
	print(stderr)

