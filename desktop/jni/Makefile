TARGET = ../libevi_10.so 
OBJECTS = cv_proc.o marker.o decode.o calib.o
JNIINC = /usr/lib/jvm/java-7-openjdk-amd64/include/
CXXFLAGS = -I$(JNIINC) `pkg-config --cflags opencv` -O3 -DDESKTOP -DNDEBUG -fPIC
LDLIBS = `pkg-config --libs opencv`

all: $(TARGET)

$(TARGET): $(OBJECTS)
	g++ -o $(TARGET) -shared $(OBJECTS) $(CXXFLAGS) $(LDLIBS)

%o: %cpp

clean:
	$(RM) $(TARGET) $(OBJECTS)
