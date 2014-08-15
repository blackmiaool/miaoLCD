
/*
* RoboPeak USB LCD Display Linux Driver
*
* Copyright (C) 2009 - 2013 RoboPeak Team
* This file is licensed under the GPL. See LICENSE in the package.
*
* http://www.robopeak.net
*
* Author Shikai Chen
* -----------------------------------------------------------------
* USB Driver Implementations
*/

#include "inc/common.h"
#include "inc/usbhandlers.h"
#include "inc/fbhandlers.h"
#include "inc/touchhandlers.h"
#include "linux/mutex.h"
#define RPUSBDISP_STATUS_BUFFER_SIZE 32


struct wifi_fb_info{
    int x;
    int y;
    int right;
    int bottom;
    int line_width;
    u16 buf[RP_DISP_DEFAULT_WIDTH*RP_DISP_DEFAULT_HEIGHT];
};
struct rpusbdisp_dev {
    void * fb_handle;
    void * touch_handle;
    struct delayed_work completion_work;
    struct cdev cdev;
    struct wifi_fb_info fb_info;
    struct mutex resource;
    struct mutex reading;
    bool in_use;
    pixel_type_t *framebuffer;
    
};
int wifidisp_open(struct inode *inode,struct file *filp);
int wifidisp_release(struct inode *inode,struct file *filp);
ssize_t wifidisp_read(struct file *filp,char __user *buf, size_t count, \
loff_t* f_pos);
struct file_operations wifidisp_fops={
    .owner=THIS_MODULE,
    .read=wifidisp_read,
    .open=wifidisp_open,
    .release=wifidisp_release  
};

int wifidisp_open(struct inode *inode,struct file *filp)
{
    struct rpusbdisp_dev *wifidispp;
    wifidispp=container_of(inode->i_cdev,struct rpusbdisp_dev,cdev);
    filp->private_data=wifidispp;
    wifidispp->in_use=true;
    return 0;
}
int wifidisp_release(struct inode *inode,struct file *filp)
{
    struct rpusbdisp_dev *wifidispp;
    wifidispp=container_of(inode->i_cdev,struct rpusbdisp_dev,cdev);
    wifidispp->in_use=false;
    printk("release");
    return 0;
}
ssize_t wifidisp_read(struct file *filp,char __user *buf, size_t count, \
loff_t* f_pos)
{
    //printk(KERN_NOTICE "read");
   // return 1;
    int result;
    int i=0;
    struct rpusbdisp_dev *wifidispp;
    struct wifi_fb_info *fb_infop;
    
    //pixel_type_t *framebuffer;
    size_t image_size;// = (fb_infop->right-fb_infop->x + 1)* (fb_infop->bottom-fb_infop->y+1) * (RP_DISP_DEFAULT_PIXEL_BITS/8);
    wifidispp=(struct rpusbdisp_dev*)filp->private_data;
    //framebuffer=wifidispp->buf;
    mutex_lock(&wifidispp->resource);
    fb_infop=&wifidispp->fb_info;
    image_size = (fb_infop->right-fb_infop->x + 1)* (fb_infop->bottom-fb_infop->y+1) * (RP_DISP_DEFAULT_PIXEL_BITS/8);

    result=copy_to_user(buf,(char *)fb_infop,image_size+20);
    *f_pos+=(image_size+20);
    mutex_unlock(&wifidispp->reading);
    return image_size+20;
}


void rpusbdisp_usb_set_fbhandle(struct rpusbdisp_dev * dev, void * fbhandle)
{
    dev->fb_handle = fbhandle;
}
 
void * rpusbdisp_usb_get_fbhandle(struct rpusbdisp_dev * dev)
{
    return dev->fb_handle;
}

void rpusbdisp_usb_set_touchhandle(struct rpusbdisp_dev * dev, void * touch_handle)
{
    dev->touch_handle = touch_handle;
}

void * rpusbdisp_usb_get_touchhandle(struct rpusbdisp_dev * dev)
{
    return dev->touch_handle;
}

static void _on_display_transfer_finished_delaywork(struct work_struct *work)
{
    struct rpusbdisp_dev * dev = container_of(work, struct rpusbdisp_dev,
completion_work.work);

    fbhandler_on_all_transfer_done(dev);
}


int rpusbdisp_usb_try_copy_area(struct rpusbdisp_dev * dev, int sx, int sy, int dx, int dy, int width, int height)
{
    printk("!!!!!rpusbdisp_usb_try_copy_area!!!!\n");
    return 1;

}

int rpusbdisp_usb_try_draw_rect(struct rpusbdisp_dev * dev, int x, int y, int right, int bottom, pixel_type_t color, int operation)
{
    printk("!!!!!rpusbdisp_usb_try_draw_rect!!!!\n");
    return 1;

}

int rpusbdisp_usb_try_send_image(struct rpusbdisp_dev * dev, const pixel_type_t * framebuffer, int x, int y, int right, int bottom, int line_width, int clear_dirty)
{

    int ix,iy,index=0;
    //int last_copied_x, last_copied_y;

    // estimate how many tickets are needed
    const size_t image_size = (right-x + 1)* (bottom-y+1) * (RP_DISP_DEFAULT_PIXEL_BITS/8);
    //printk("buffer=%d\n",(int)framebuffer);
    // printk("imaage\n");
     //mutex_unlock(resource);
    // printk("1sx=%d",x);
    // printk("ly=%d",y);
    // printk("lright=%d",right);
    // printk("lbottom=%d",bottom);
    // printk("lline_width=%d",line_width);
    // do not transmit zero size image
    //printk("send_image\n");
    if (!image_size) return 1;
    if(dev->in_use==false) return 0;
    //dev->framebuffer=(pixel_type_t *)framebuffer;//+y*line_width + x;
    dev->fb_info.x=x;
    dev->fb_info.y=y;
    dev->fb_info.right=right;
    dev->fb_info.bottom=bottom;
    dev->fb_info.line_width=line_width;

    framebuffer+=(y*line_width+x);
    for(iy=y;iy<=bottom;++iy)
    {
        for(ix=x;ix<=right;++ix)
        {
            dev->fb_info.buf[index]=*framebuffer;
            index++;
            framebuffer++;
        }
        framebuffer+=line_width-right-1+x;
    }

    mutex_unlock(&dev->resource);
    //printk("send_2\n");
    mutex_lock(&dev->reading);

    return 1;
}

static int rp_init(void);
static int rp_init()
{
    int scull_major=0;
    struct rpusbdisp_dev *wifidispp = NULL;
    dev_t dev;
    int result;

    result=alloc_chrdev_region(&dev,0,1,"fb_wifi");
    scull_major=MAJOR(dev);
    
    /* allocate memory for our device state and initialize it */
    wifidispp = kzalloc(sizeof(*wifidispp), GFP_KERNEL);
    
    if (wifidispp == NULL) {
    err("Out of memory");
    goto error;
    }

    mutex_init(&wifidispp->resource);
    mutex_init(&wifidispp->reading);
    mutex_lock(&wifidispp->resource);
    mutex_lock(&wifidispp->reading);


    cdev_init(&wifidispp->cdev,&wifidisp_fops);
    wifidispp->cdev.owner=THIS_MODULE;
    result=cdev_add(&wifidispp->cdev,MKDEV(scull_major,0),1);
    if(result)
        goto error;



    INIT_DELAYED_WORK(&wifidispp->completion_work, _on_display_transfer_finished_delaywork);
    fbhandler_on_new_device(wifidispp);
    touchhandler_on_new_device(wifidispp);
    fbhandler_set_unsync_flag(wifidispp);
    schedule_delayed_work(&wifidispp->completion_work, 0);

    return 0;

    error:
        if (wifidispp) {
            kfree(wifidispp);
        }
    return -1;

}


int __init register_usb_handlers(void)
{


    rp_init();
    return 0;
}


void unregister_usb_handlers(void)
{

}
