#ifndef _JPG_H_  
#define _JPG_H_
typedef unsigned char u8;
typedef unsigned short u16;
typedef unsigned long u32;
int img_init(void);
int get_img(int fp,u8 *buf,u8 whole);
#define WIDTH  854
#define HEIGHT 480
#define BITS_PER_FB 16
#endif//_JPG_H_

