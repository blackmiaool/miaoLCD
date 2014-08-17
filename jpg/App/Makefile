
default:wifi_jpg.o jpg16.o
	cc -g wifi_jpg.o jpg16.o -ljpeg
wifi_jpg.o:wifi_jpg.c jpg.h
	cc -g -c wifi_jpg.c
jpg16.o:jpg16.c jpg.h
	cc -g -c jpg16.c

clean:
	rm *.o

