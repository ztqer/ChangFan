using System;
using System.Windows.Forms;
using System.Drawing;
using System.Drawing.Printing;
using System.Net.Sockets;
using System.Net;
using System.Text;
using System.Threading;
using System.Collections.Generic;
using System.Drawing.Imaging;

namespace PrinterController
{
    public partial class Form1 : Form
    {
        //更新数据相关
        private SynchronizationContext context;
        private List<Order> orders = new List<Order>();
        private List<double> price = new List<double>();
        private int currentRow;
        private string client;
        private int orderCount;
        private int orderNum;
        private int clothNum;
        private double money;
        //打印相关
        private PrintDocument printDocument=new PrintDocument();
        private Bitmap memoryImage;
        private PrintPreviewDialog dialog = new PrintPreviewDialog();
        public Form1()
        {
            InitializeComponent();
            context = SynchronizationContext.Current;
            printDocument.PrintPage += new PrintPageEventHandler(printDocument_PrintPage);
            InitializeTable();
            Thread thread = new Thread(ListeToBroadcast);
            thread.Start();
        }

        private void Form1_FormClosing(object sender, FormClosingEventArgs e)
        {
            Environment.Exit(0);
        }

        //初始化表格
        private void InitializeTable()
        {
            for(int i = 1; i <= 13; i++)
            {
                dataGridView1.Rows.Add();
            }
        }

        //用于测试
        private void button1_Click(object sender, EventArgs e)
        {
            ApplyMessage("print/客户/2/12.0/13.0");
        }

        //开启socket接收服务器广播
        private void ListeToBroadcast()
        {
            Socket socket = new Socket(AddressFamily.InterNetwork, SocketType.Stream, ProtocolType.Tcp);
            socket.Connect(IPAddress.Parse("49.234.85.96"),2414);
            byte[] buffer = new byte[1024];
            string s1 = "Broadcast";
            socket.Send(Encoding.UTF8.GetBytes(s1));
            int len1=socket.Receive(buffer);
            string s2 = Encoding.UTF8.GetString(buffer, 0,len1);
            if (s2.Equals("请继续"))
            {
                while (true)
                {
                    int len2=socket.Receive(buffer);
                    if (len2 > 1)
                    {
                        string message = Encoding.UTF8.GetString(buffer, 0, len2);
                        ApplyMessage(message);
                    }
                    socket.Send(new byte[1]);
                }
            }
            socket.Close();
        }

        //筛选消息并应用
        private void ApplyMessage(string s)
        {
            //添加订单
            if(s.Length > 14 && s.Substring(0, 14).Equals("record/update/"))
            {
                string order = s.Substring(14);
                Order o = new Order(order);
                orders.Add(o);
                context.Post(UpdateUI, o);
                return;
            }
            //读取客户名与订单总数
            if (s.Length>6 && s.Substring(0,6).Equals("print/"))
            {
                //根据斜杠分割字符串
                List<string> list = new List<string>();
                int begin = 0;
                int count = 0;
                for (int i = 0; i <= s.Length - 2; i++)
                {
                    if (s.Substring(i, 1).Equals("/"))
                    {
                        list.Add(s.Substring(begin, count));
                        begin = i + 1;
                        count = 0;
                    }
                    else
                    {
                        count++;
                    }
                }
                list.Add(s.Substring(begin));
                client = list[1];
                orderCount = int.Parse(list[2]);
                for(int i = 1; i <= orderCount; i++)
                {
                    price.Add(double.Parse(list[i + 2]));
                }
            }
        }

        //更新UI
        private void UpdateUI(object order)
        {
            try {
                Order o = (Order)order;
                clothNum += o.numbers.Count;
                if (label5.Text.Equals("订单号："))
                {
                    label5.Text += o.orderId;
                }
                dataGridView1.Rows[currentRow].Cells[0].Value = o.clothId;
                dataGridView1.Rows[currentRow].Cells[1].Value = o.color;
                dataGridView1.Rows[currentRow].Cells[12].Value = o.numCount + o.unit;
                dataGridView1.Rows[currentRow].Cells[13].Value = price[orderNum];
                dataGridView1.Rows[currentRow].Cells[14].Value = price[orderNum] * o.numCount;
                money += price[orderNum] * o.numCount;
                int x = 0;
                for (int i = 0; i <= o.numbers.Count - 1; i++)
                {
                    dataGridView1.Rows[currentRow].Cells[2 + i - x].Value = o.numbers[i];
                    if (2 + i == 11)
                    {
                        x += 10;
                        currentRow++;
                    }
                }
                currentRow++;
                orderNum++;
                if (orderNum == orderCount)
                {
                    context.Post(LastUpdateAndPrint, client);
                }
            }
            catch(Exception e)
            {
                throw e;
                this.Text = "订单超出上限，请重新启动程序";
                label9.Text = "订单超出上限，请重新启动程序";
                label9.Visible = true;
                //Thread t = new Thread(Exit);
                //t.Start();
            }
        }

        //延时5s关闭程序
        private void Exit()
        {
            Thread.Sleep(5000);
            Environment.Exit(0);
        }

        //接收到完成信号，更新剩余UI并打印
        private void LastUpdateAndPrint(object message)
        {
            label4.Text += (string)message;
            label6.Text+= DateTime.Now.ToLongDateString().ToString();
            label7.Text += clothNum + "匹    " +money+ "元";
            //强制重绘，否则会截屏未更改的界面
            this.Refresh();
            Print();
            //重新初始化控件与数据
            foreach (DataGridViewRow row in dataGridView1.Rows)
            {
                foreach (DataGridViewCell cell in row.Cells)
                {
                    cell.Value = "";
                }
            }
            orders.Clear();
            price.Clear();
            currentRow = 0;
            client = null;
            orderCount = 0;
            orderNum = 0;
            clothNum = 0;
            money = 0d;
            label4.Text = "客户：";
            label5.Text = "订单号：";
            label6.Text = "日期：";
            label7.Text = "共计：";
        }

        //订单类
        private class Order
        {
            public string orderId;
            public string clothId;
            public string color;
            public string unit;
            public double numCount;
            public List<string> numbers = new List<string>();
            public Order(string s)
            {
                //根据斜杠分割字符串
                List<string> list = new List<string>();
                int begin = 0;
                int count = 0;
                for (int i = 0; i <= s.Length - 2; i++)
                {
                    if (s.Substring(i, 1).Equals("/"))
                    {
                        list.Add(s.Substring(begin, count));
                        begin = i + 1;
                        count = 0;
                    }
                    else
                    {
                        count++;
                    }
                }
                list.Add(s.Substring(begin));
                orderId = list[0];
                clothId = list[1];
                color = list[2];
                unit = list[3];
                for (int i = 4; i <= list.Count - 1; i++)
                {
                    numbers.Add(list[i]);
                    numCount += double.Parse(list[i]);
                }
            }
        }

        //使窗体置顶，然后截取窗口部分屏幕并打印
        private void Print()
        {
            this.BringToFront();
            CaptureScreen();
            printDocument.DefaultPageSettings.Landscape = true;
            printDocument.Print();
            //dialog.Document = printDocument;
            //dialog.ShowDialog();
        }

        private void CaptureScreen()
        {
            Graphics myGraphics = this.CreateGraphics();
            this.Location = new Point(150, 150);
            //适应系统缩放设置，计算工作区大小
            Size s = new Size((int)(((float)this.Width) * (1064f / 1080f) *ScreenSettingUtility.ScaleX), (int)(((float)this.Height) * (526f / 565f) * ScreenSettingUtility.ScaleY));
            memoryImage = new Bitmap(s.Width, s.Height, myGraphics);
            Graphics memoryGraphics = Graphics.FromImage(memoryImage);
            memoryGraphics.CopyFromScreen(PointToScreen(label9.Location), new Point(0,0), s); 
            //保存图片至c盘 changfan文件夹
            //memoryImage.Save(@"C:\VSProjects\test.jpg", ImageFormat.Jpeg);
            memoryImage.Save(@"C:\changfan\"+orders[0].orderId+".jpg", ImageFormat.Jpeg);
        }

        private void printDocument_PrintPage(object sender,
               PrintPageEventArgs e)
        {
            //超出打印纸张大小则进行缩放
            Size s = new Size((int)(((float)this.Width) * (1064f / 1080f) * ScreenSettingUtility.ScaleX), (int)(((float)this.Height) * (526f / 565f) * ScreenSettingUtility.ScaleY));
            int w = (int)printDocument.DefaultPageSettings.Bounds.Width;
            int h = (int)printDocument.DefaultPageSettings.Bounds.Height;
            float scale1 = (float)s.Width / (float)w;
            float scale2 = (float)s.Height / (float)h;
            if (scale1 > 1f && scale1 >= scale2)
            {
                h = (int)((float)s.Height / scale1);
            }
            else if(scale2 > 1f && scale2 > scale1)
            {
                w = (int)((float)s.Width / scale2);
            }
            e.Graphics.DrawImage(memoryImage, 0, 0,w,h);
        }
    }
}
