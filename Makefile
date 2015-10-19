PACKR = java -jar /home/miky/Descargas/packr.jar
PACKR_CONF = packr.json

all:
	cd desktop/jni; $(MAKE) $(MFLAGS)
	cd ../..
	./gradlew desktop:dist
	$(PACKR) $(PACKR_CONF)
