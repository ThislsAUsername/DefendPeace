
import sys
import math
import re
import csv
import glob


def convertFile(infile,outfile):
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
	outfile.write(outstring)
	return

def main(names):
	for name in names:
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

def indexToTerrainCode(x):
	return {
		1:   '  GR',#plains/grass
		2:   '  MT',#mountain
		3:   '  FR',#woods/forest
		4:   '  XX',#rivers
		5:   '  XX',
		6:   '  XX',
		7:   '  XX',
		8:   '  XX',
		9:   '  XX',
		10:  '  XX',
		11:  '  XX',
		12:  '  XX',
		13:  '  XX',
		14:  '  XX',
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
		26:  '  RD',#HBridge
		27:  '  RD',#VBridge
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
		40:  ' 0XX',
		41:  ' 0XX',
		42:  ' 0HQ',
		43:  ' 1CT',
		44:  ' 1FC',
		45:  ' 1XX',
		46:  ' 1XX',
		47:  ' 1HQ',
		48:  ' 2CT',
		49:  ' 2FC',
		50:  ' 2XX',
		51:  ' 2XX',
		52:  ' 2HQ',
		53:  ' 3CT',
		54:  ' 3FC',
		55:  ' 3XX',
		56:  ' 3XX',
		57:  ' 3HQ',
		81:  ' 5CT',
		82:  ' 5FC',
		83:  ' 5XX',
		84:  ' 5XX',
		85:  ' 5HQ',
		86:  ' 6CT',
		87:  ' 6FC',
		88:  ' 6XX',
		89:  ' 6XX',
		90:  ' 6HQ',
		91:  ' 4CT',
		92:  ' 4FC',
		93:  ' 4XX',
		94:  ' 4XX',
		95:  ' 4HQ',
		96:  ' 7CT',
		97:  ' 7FC',
		98:  ' 7XX',
		99:  ' 7XX',
		100: ' 7HQ',
		101: '  XX',#pipe
		102: '  XX',
		103: '  XX',
		104: '  XX',
		105: '  XX',
		106: '  XX',
		107: '  XX',
		108: '  XX',
		109: '  XX',
		110: '  XX',
		111: '  XX',#silo
		112: '  XX',#used silo
		113: '  XX',#HPipe seam
		114: '  XX',#VPipe seam
		115: '  XX',#HPipe seam broken
		116: '  XX',#VPipe seam broken
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
		127: ' 8XX',#Com Towers
		128: ' 4XX',
		129: ' 1XX',
		130: ' 7XX',
		131: ' 2XX',
		132: ' 9XX',
		133: '  XX',
		134: ' 0XX',
		135: ' 5XX',
		136: ' 3XX',
		137: ' 6XX',
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
		152: '10XX',#Com Tower
		153: '10HQ',#HQ
		154: '10LB',#lab
		155: '10SP',#port
		156: '11AP',
		157: '11FC',
		158: '11CT',
		159: '11XX',
		160: '11HQ',
		161: '11LB',
		162: '11SP',
		163: '12AP',
		164: '12FC',
		165: '12CT',
		166: '12XX',
		167: '12HQ',
		168: '12LB',
		169: '12SP',
		170: '13AP',
		171: '13FC',
		172: '13CT',
		173: '13XX',
		174: '13HQ',
		175: '13LB',
		176: '13SP',
	}.get(x, '  XX')    # '  XX' is default if x not found
	
	

if __name__ == "__main__":
	helpstring = "Takes in Advance Wars by Web map files, and outputs our own map format. \nFilename is the same, extension is '.map'.\nYou can input the filenames as command line arguments, or you can drag'n'drop the files you want to convert in your file browser."
	if "-h" in sys.argv or "--help" in sys.argv:
		print(helpstring)
		input()
	elif len(sys.argv) < 2:
		filesToParse = glob.glob('*.txt')
		print("No input given - processing all *.txt files:")
		main(filesToParse)
	else:
		main(sys.argv[1:])
