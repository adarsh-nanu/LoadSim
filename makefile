CLASSPATH=libs\log4j-1.2.17.jar

exe	:	JPOS.class

JPOS.class:
	javac -cpchannel\JPOS.java -d bin