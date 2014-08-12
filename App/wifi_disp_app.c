#include <stdio.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <unistd.h>
#include <linux/in.h>
#include <stdlib.h>

#include <fcntl.h>
#define width  320   
#define height 240
#define PORT   8081
typedef unsigned char u8;
typedef unsigned short u16;
typedef unsigned long u32;
struct wifi_fb_info{
  int index;
  int x;
  int y;
  int right;
  int bottom;
  int line_width;
  u8 fb_buf[width*height*2];
}fb_info;

int main()
{
  int fp;
  u32 get_cnt;
  u32 i=0;
  fp=open("/dev/miao",O_RDONLY);
  if(!fp)
  {
    printf("open failed\r\n");
    return 0;
  }
  else
    printf("open sucess,size=%d\r\n",sizeof(fb_info));

  for(;;)
    {
      u8 *hand_addr=(u8 *)&fb_info;
      get_cnt=read(fp,hand_addr+4,sizeof(fb_info));
      if(get_cnt>0)
      {

	  for(i=0;i+30000<get_cnt;i+=30000)
	  {
	    hand_addr[i]=i/30000;
	    	    fb_send(hand_addr+i,30004);
	    //fb_send(hand_addr+i,5);
	  }
	  hand_addr[i]=i/30000;
	  	  fb_send(hand_addr+i,get_cnt-i+4);
		  //fb_send(hand_addr+i,5);
      }
      
      printf("get_cnt=%ld\n",get_cnt);
      printf("x=%d",fb_info.x);
      printf("y=%d",fb_info.y);
      printf("right=%d",fb_info.right);
      printf("bottom=%d",fb_info.bottom);
      printf("line_width=%d",fb_info.line_width);
      printf("color%d",fb_info.fb_buf[100]);
    }
  close(fp);
  return 0;
}
void fb_send(u8 *buf,u32 cnt)// cnt <=60000
{
  int skt;
  struct sockaddr_in server_addr,client_addr;
  printf("send %d B;",cnt);
  skt=socket(AF_INET,SOCK_DGRAM,0);
  if(skt<0){
    printf("eeeeeeeeeeerrrrrrrrrrrrrr\n");
    perror("socket error!\n");
    return ;
  }
  memset(&server_addr,0,sizeof(server_addr));
  server_addr.sin_family=AF_INET;
  server_addr.sin_port=htons(PORT);
  server_addr.sin_addr.s_addr=inet_addr("192.168.123.3");
  struct sockaddr_in from;
  sendto(skt,buf,cnt,0,(struct sockaddr *)&server_addr,sizeof(server_addr));
  printf("send done\n");
}



