#include<stdio.h>
#include<stdlib.h>
#include<sys/ioctl.h>
#include<unistd.h>
#include<fcntl.h>
#include <malloc.h>
#include <sys/time.h> 
#include <time.h>
#include<linux/fb.h>
#include <jpeglib.h>
#include <jerror.h>
#include "jpg.h"

#define BITS_PER_FB 16
/*RGB565转RGB24函数
 *@rgb565: 指向存放rgb565数据的起始地址
 *@rgb24：指向存放rgb24数据的起始地址
 *@width：屏幕（分辨率）的宽度
 *@height：屏幕（分辨率）的高度
 */
int RGB565_to_RGB24(unsigned char *rgb565,unsigned char *rgb24,int width,int height)
{
    int i;
    int whole = width*height;
    unsigned char r,g,b;
    unsigned short int *pix565;

    pix565 = (unsigned short int *)rgb565;

    for(i = 0;i < whole;i++)
    {
        int pix=*pix565;
        /*         r = ((*pix565)>>11)&0x1f;
         *rgb24 = (r<<3) | (r>>2);
         rgb24++;
         g = ((*pix565)>>5)&0x3f;
         *rgb24 = (g<<2) | (g>>4);
         rgb24++;
         b = (*pix565)&0x1f;
         *rgb24 = (b<<3) | (b>>2);
         rgb24++;
       */
        *(unsigned int *)rgb24=((pix&0xf800)<<8)|((pix&0x7e0)<<5)|((pix&0x1f)<<3);
        rgb24+=3;
        pix565++;

    }
    return 1;
}

/*jpeg压缩函数
 *@rgb：指向存放rgb24数据的起始地址
 *@width：屏幕（分辨率）的宽度
 *@height：屏幕（分辨率）的高度
 */
int jpeg_compress(unsigned char *rgb,int width,int height)
{
    struct jpeg_compress_struct cinfo;
    struct jpeg_error_mgr jerr;
    FILE * outfile;
    JSAMPROW row_pointer[1];
    int row_stride;
    cinfo.err = jpeg_std_error(&jerr);
    jpeg_create_compress(&cinfo);
    //输出文件名为：out.jpg
    if ((outfile = fopen("out.jpg", "wb")) == NULL)
    {
        printf("can not open out.jpg\n");
        return -1;
    }
    jpeg_stdio_dest(&cinfo, outfile);

    cinfo.image_width = width;
    cinfo.image_height = height;
    cinfo.input_components = 3;
    //输入数据格式为RGB
    cinfo.in_color_space = JCS_RGB;

    jpeg_set_defaults(&cinfo);
    //压缩质量为80
    jpeg_set_quality(&cinfo, 80, TRUE );
    jpeg_start_compress(&cinfo, TRUE);
    row_stride = width * 3;

    while (cinfo.next_scanline < cinfo.image_height)
    {
        row_pointer[0] = &rgb[cinfo.next_scanline * row_stride];
        (void) jpeg_write_scanlines(&cinfo, row_pointer, 1);
    }

    jpeg_finish_compress(&cinfo);
    fclose(outfile);

    jpeg_destroy_compress(&cinfo);

    return 1;
}
static int fd;//fb device file
static unsigned char *trgb;
int img_init()
{
    fd = open("/dev/fb2",O_RDONLY);
    if(fd < 0)
    {
        printf("can not open fb dev\n");
        return -1;
    }
    trgb = (unsigned char *)malloc(WIDTH*HEIGHT*3);
    if(trgb==NULL)
        exit(0);
    return 0;
}

int get_img(int fp,u8 *buf)
{

    int fd;

    int buffer_size;
    //    struct timeval starttime,endtime;
    //    fp=fp;
    //打开framebuffer设备

    buffer_size = (WIDTH * HEIGHT * BITS_PER_FB/8);

    //获取一帧数据

    if(read(fd,trgb,buffer_size) < 0)
    {
        printf("reaf failed!\n");
        return 0;
    }
    //格式转换
    //    gettimeofday(&endtime,0);
    RGB565_to_RGB24(trgb,buf,WIDTH,HEIGHT);
    //    gettimeofday(&starttime,0);


    //jpeg压缩
    if(jpeg_compress(buf,WIDTH,HEIGHT)<0)
        printf("compress failed!\n");

    //    double timeuse = 1000000*(endtime.tv_sec - starttime.tv_sec) + endtime.tv_usec - starttime.tv_usec;

    //    printf("timeuse=%f\n",timeuse);

    return 0;
}


