#include <stdio.h>
#include <stdlib.h> 
#include <unistd.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <unistd.h>
#include <linux/in.h>
#include <stdlib.h>
#include <memory.h>
#include <fcntl.h>
#include "unistd.h"
#include "jpg.h"

#define PORT   8090
#define PKG_MAX 40000L
#define DELAY_US 40000L

struct wifi_fb_info{
    int index;
    int x;
    int y;
    int right;
    int bottom;
    int line_width;
    u8 fb_buf[WIDTH*HEIGHT*2];
}fb_info;
int skt;
struct sockaddr_in server_addr,client_addr;
void fb_send(u8 *buf,u32 cnt);// cnt <=60000

int main()
{
    int fp;
    u32 get_cnt;
    u32 i=0;
    if(img_init())
    {
        printf("image init fault\n");
        return -1;
    }

    skt=socket(AF_INET,SOCK_DGRAM,0);
    if(skt<0){
        printf("eeeeeeeeeeerrrrrrrrrrrrrr\n");
        perror("socket error!\n");
        return -1;
    }
    memset(&server_addr,0,sizeof(server_addr));
    server_addr.sin_family=AF_INET;
    server_addr.sin_port=htons(PORT);
    server_addr.sin_addr.s_addr=inet_addr("192.168.1.100");

    fp=open("/dev/miao",O_RDONLY);
    if(fp<0)
    {
        printf("open failed\r\n");
		//        return 0;
    }
    else
        printf("open sucess,size=%d\r\n",sizeof(fb_info));

    for(;;)
    {
        u8 *hand_addr=(u8 *)&fb_info;
        static u8 index=0;
        get_cnt=get_img(fp,(u8 *)hand_addr+4,0);
	
        if(get_cnt>0)
        {
            hand_addr[1]=1;//start flag
            hand_addr[2]=index;

            for(i=0;i+PKG_MAX<get_cnt;i+=PKG_MAX)
            {
                hand_addr[i]=i/PKG_MAX;
                if(i!=0)
                    hand_addr[i+1]=0;
                hand_addr[i+2]=index;
                fb_send(hand_addr+i,PKG_MAX+4);

                usleep(DELAY_US);
            }
            hand_addr[i+1]=2;//end flag
            hand_addr[i+2]=index;
            hand_addr[i]=i/PKG_MAX;
			printf("index=%d\n",index);
            index++;
            fb_send(hand_addr+i,get_cnt-i+4);
            //fb_send(hand_addr+i,5);
			usleep(DELAY_US);



        }

        //      printf("get_cnt=%ld\n",get_cnt);
        //      printf("x=%d",fb_info.x);
        //      printf("y=%d",fb_info.y);
        //      printf("right=%d",fb_info.right);
        //      printf("bottom=%d",fb_info.bottom);
        //      printf("line_width=%d",fb_info.line_width);
        //      printf("color%d",fb_info.fb_buf[100]);
    }
    close(fp);
    return 0;
}

void fb_send(u8 *buf,u32 cnt)// cnt <=60000
{
	printf("send %d B;",(int)cnt);
    struct sockaddr_in from;
    sendto(skt,buf,cnt,0,(struct sockaddr *)&server_addr,sizeof(server_addr));
    printf("send done\n");
}



