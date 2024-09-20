#!/usr/bin/python3

import sys
import math
import re
import csv
import glob

try:
	from http import client
	import ssl
	import json
	global context
	context = ssl.create_default_context()
except:
	print("Note: Seems like you aren't using Python 3.5+, so putting in map IDs won't work")


def convertFile(infile,outfile):
	'''
	Turn the input csv file into a Defend Peace style map and dump it to the provided file.
	'''
	reader = csv.reader(infile)
	# We write all of our output text to an intermediate string, since that is much more efficient than directly appending to the file.
	outstring = ""
	for line in reader:
		for num in line:
			try:
				num = int(num)
			# Teleporter tiles are a blank value in AWBW, so having -1 is a desired behavior for if/when we implement those.
			except ValueError:
				num = -1
			outstring += indexToTerrainCode(num)
		outstring += "\n"
	outstring += "team, unit type, x, y\n\n"
	outfile.write(outstring)
	return


def convertAPI(mapID):
	'''
	Snag the map's data from the AWBW API and dump it to a file.
	'''
	try:
		global context
		req = client.HTTPSConnection("awbw.amarriner.com", 443, context=context)
		req.connect()
		req.request("GET", "/api/map/map_info.php?maps_id=" + mapID)
		response = req.getresponse()
		responseData = response.read()
		jsonData = json.loads(responseData)
		req.close()
		mapName = jsonData["Name"]
		outname = (mapName + ".map").replace(' ', '_')

		try:
			# If someone's running it from shell, and it pukes, they'll at least know which map made it puke.
			print("Now converting file:", mapName)
			outfile = open(outname, 'w')
			convertJSON(jsonData,outfile)
		except IOError:
			print("File ",outname," cannot be written to.")
		finally:
			outfile.close()

	except Exception as ex:
		print("Can't pull map from AWBW:", ex)
		raise ex
	finally:
		req.close()

	return

def convertJSON(jsonData,outfile):
	'''
	Take the JSON map data and dump it into the provided file.
	'''
	outstring = ""
	mapBodyLines = ["" for x in range(len(jsonData["Terrain Map"][0]))]

	for column in jsonData["Terrain Map"]:
		lineID = 0
		for num in column:
			try:
				num = int(num)
			# Teleporter tiles are a blank value in AWBW, so having -1 is a desired behavior for if/when we implement those.
			except ValueError:
				num = -1
			mapBodyLines[lineID] += indexToTerrainCode(num)
			lineID += 1

	for line in mapBodyLines:
		outstring += line + "\n"
	outstring += f"team, unit type, x, y (author: {jsonData['Author']})\n"
	for unit in jsonData["Predeployed Units"]:
		player = countryCodeToPlayerID( unit['Country Code'] )
		stringID = unitIDToString(unit['Unit ID'])
		outstring += f"{player}, {stringID}, {unit['Unit X']}, {unit['Unit Y']}\n"
	outfile.write(outstring)
	return


def main(names):
	for name in names:
		if name.isdigit():
			convertAPI(name)
			continue

		try:
			infile = open(name)
			# Split the file extension off, and replace it with .map
			outname = re.sub(r'\..*$','.map',name).replace(' ', '_')
			try:
				# If someone's running it from shell, and it pukes, they'll at least know which map made it puke.
				print("Now converting file:", name)
				outfile = open(outname, 'w')
				convertFile(infile,outfile)
			except IOError:
				print("File ",outname," cannot be written to.")
		except IOError:
			print("File ",name," does not exist.")
		finally:
			infile.close()
			outfile.close()

def countryCodeToPlayerID(x):
	return {
		'os':  0,
		'bm':  1,
		'ge':  2,
		'yc':  3,
		'bh':  4,
		'rf':  5,
		'gs':  6,
		'bd':  7,
		'ab':  8,
		'js':  9,
		'ci': 10,
		'pc': 11,
		'tg': 12,
		'pl': 13,
		'ar': 14,
		'wn': 15,
	}.get(x, 0)

def unitIDToString(x):
	return {
		1:       'infantry',
		2:       'mech',
		3:       'md tank',
		4:       'tank',
		5:       'recon',
		6:       'apc',
		7:       'artillery',
		8:       'rockets',
		9:       'anti-air',
		10:      'missiles',
		11:      'fighter',
		12:      'bomber',
		13:      'b-copter',
		14:      't-copter',
		15:      'battleship',
		16:      'cruiser',
		17:      'lander',
		18:      'sub',
		28:      'bboat',
		29:      'carrier',
		30:      'stealth',
		46:      'neotank',
		960900:  'piperunner',
		968731:  'bbomb',
		1141438: 'megatank',
	}.get(x, 'oozium')

def indexToTerrainCode(x):
	return {
		-1:  '  TT',#Teletiles
		1:   '  GR',#plains/grass
		2:   '  MT',#mountain
		3:   '  FR',#woods/forest
		4:   '  RV',#rivers
		5:   '  RV',
		6:   '  RV',
		7:   '  RV',
		8:   '  RV',
		9:   '  RV',
		10:  '  RV',
		11:  '  RV',
		12:  '  RV',
		13:  '  RV',
		14:  '  RV',
		15:  '  RD',#road
		16:  '  RD',
		17:  '  RD',
		18:  '  RD',
		19:  '  RD',
		20:  '  RD',
		21:  '  RD',
		22:  '  RD',
		23:  '  RD',
		24:  '  RD',
		25:  '  RD',
		26:  '  BR',#HBridge
		27:  '  BR',#VBridge
		28:  '  SE',#sea
		29:  '  SH',#shoal
		30:  '  SH',
		31:  '  SH',
		32:  '  SH',
		33:  '  RF',#reef
		34:  '  CT',#city
		35:  '  FC',#base/factory
		36:  '  AP',#airport
		37:  '  SP',#port
		38:  ' 0CT',
		39:  ' 0FC',
		40:  ' 0AP',
		41:  ' 0SP',
		42:  ' 0HQ',
		43:  ' 1CT',
		44:  ' 1FC',
		45:  ' 1AP',
		46:  ' 1SP',
		47:  ' 1HQ',
		48:  ' 2CT',
		49:  ' 2FC',
		50:  ' 2AP',
		51:  ' 2SP',
		52:  ' 2HQ',
		53:  ' 3CT',
		54:  ' 3FC',
		55:  ' 3AP',
		56:  ' 3SP',
		57:  ' 3HQ',
		81:  ' 5CT',
		82:  ' 5FC',
		83:  ' 5AP',
		84:  ' 5SP',
		85:  ' 5HQ',
		86:  ' 6CT',
		87:  ' 6FC',
		88:  ' 6AP',
		89:  ' 6SP',
		90:  ' 6HQ',
		91:  ' 4CT',
		92:  ' 4FC',
		93:  ' 4AP',
		94:  ' 4SP',
		95:  ' 4HQ',
		96:  ' 7CT',
		97:  ' 7FC',
		98:  ' 7AP',
		99:  ' 7SP',
		100: ' 7HQ',
		101: '  PI',#pipe
		102: '  PI',
		103: '  PI',
		104: '  PI',
		105: '  PI',
		106: '  PI',
		107: '  PI',
		108: '  PI',
		109: '  PI',
		110: '  PI',
		111: '  SR',#silo (SR, for Silo Ready)
		112: '  BK',#used silo
		113: '  ME',#HPipe seam
		114: '  ME',#VPipe seam
		115: '  GR',#HPipe seam broken
		116: '  GR',#VPipe seam broken
		117: ' 8AP',#airport
		118: ' 8FC',#base/factory
		119: ' 8CT',#city
		120: ' 8HQ',#HQ
		121: ' 8SP',#port
		122: ' 9AP',
		123: ' 9FC',
		124: ' 9CT',
		125: ' 9HQ',
		126: ' 9SP',
		127: ' 8TW',#Com Towers
		128: ' 4TW',
		129: ' 1TW',
		130: ' 7TW',
		131: ' 2TW',
		132: ' 9TW',
		133: '  TW',
		134: ' 0TW',
		135: ' 5TW',
		136: ' 3TW',
		137: ' 6TW',
		138: ' 8LB',#Labs
		139: ' 4LB',
		140: ' 1LB',
		141: ' 7LB',
		142: ' 2LB',
		143: ' 6LB',
		144: ' 9LB',
		145: '  LB',
		146: ' 0LB',
		147: ' 5LB',
		148: ' 3LB',
		149: '10AP',#airport
		150: '10FC',#base/factory
		151: '10CT',#city
		152: '10TW',#Com Tower
		153: '10HQ',#HQ
		154: '10LB',#lab
		155: '10SP',#port
		156: '11AP',#PC
		157: '11FC',
		158: '11CT',
		159: '11TW',
		160: '11HQ',
		161: '11LB',
		162: '11SP',
		163: '12AP',#TG
		164: '12FC',
		165: '12CT',
		166: '12TW',
		167: '12HQ',
		168: '12LB',
		169: '12SP',
		170: '13AP',#PL
		171: '13FC',
		172: '13CT',
		173: '13TW',
		174: '13HQ',
		175: '13LB',
		176: '13SP',
		181: '14AP',#AR
		182: '14FC',
		183: '14CT',
		184: '14TW',
		185: '14HQ',
		186: '14LB',
		187: '14SP',
		188: '15AP',#WN
		189: '15FC',
		190: '15CT',
		191: '15TW',
		192: '15HQ',
		193: '15LB',
		194: '15SP',
		195: '  TT',#Teletiles
		196: '16AP',#AA
		197: '16FC',
		198: '16CT',
		199: '16TW',
		200: '16HQ',
		201: '16LB',
		202: '16SP',
		203: '17AP',#NE
		204: '17FC',
		205: '17CT',
		206: '17TW',
		207: '17HQ',
		208: '17LB',
		209: '17SP',
	}.get(x, '  XX')    # '  XX' is default if x not found



if __name__ == "__main__":
	helpstring = "Takes in Advance Wars by Web map files (or map IDs), and outputs our own map format. \nFilename is the same, extension is '.map'.\nYou can input the filenames/IDs as command line arguments, or you can drag'n'drop the files you want to convert in your file browser."
	if "-h" in sys.argv or "--help" in sys.argv:
		print(helpstring)
		input()
	elif len(sys.argv) < 2:
		filesToParse = glob.glob('*.txt')
		print("No input given - processing all *.txt files:")
		main(filesToParse)
	else:
		main(sys.argv[1:])
