import java.util.*;
class Process
{
    int id;
    int period; //assume period is deadline
    int execution;
    int deadline;
    Process()
    {

    }
    Process(int id, int execution, int period)
    {
        this.id=id;
        this.period=period;
        this.execution=execution;
    }
}
class staticedf
{
    static int availablefreq[]={2,3,4};
    static int availablevolt[]={1,2,4};
    static ArrayList<Process> list=new ArrayList<>();
    static PriorityQueue<Process> pq=new PriorityQueue<>(new Comparator<Process>(){
        public int compare(Process p1, Process p2)
        {
            return p1.deadline-p2.deadline;
        }
    });
    public static void main(String[] args)
    {
        //energy calculation for EDF
        init();
        double f=calculateFrequency();
        f=f/4.0;
        System.out.println("Frequency is "+f);
        double current_time=0;
        while(current_time<20)
        {
            if(!pq.isEmpty())
            {
                Process p=pq.poll();
                System.out.println("Process "+p.id+" is executing from "+current_time+" to "+(current_time+1.0*p.execution/f));
                reinit(current_time, current_time+1.0*p.execution/f);
                for(Process x:pq)
                    System.out.println(x.id+" "+x.deadline);
                //System.out.println();
                current_time+=1.0*p.execution/f;
            }
            else
            {
                int nexttask=findtime(current_time,true);
                System.out.println("Idle from "+current_time+" to "+nexttask);
                current_time=nexttask;
            }
        }
    }
    static void init()
    {
        Scanner sc=new Scanner(System.in);
        int n=sc.nextInt();
        for(int i=0;i<n;i++)
        {
            int execution=sc.nextInt();
            int period=sc.nextInt();
            sc.nextInt();
            Process p=new Process(i+1, execution, period);
            p.deadline=p.period;
            list.add(p);
            pq.add(p);
        }
        sc.close();
    }
    static void reinit(double timebefore, double timeafter)
    {
        for(Process p:list)
        {
            if(((int)Math.floor(timeafter/p.period) - (int)Math.floor(timebefore/p.period ))>=1)
            {
                Process toadd=new Process(p.id, p.execution, p.period);
                toadd.deadline= (int)Math.floor(timeafter/p.period)*toadd.period + toadd.period;
                pq.add(toadd);
            }
        }
    }
    static int findtime(double current_time, boolean add)
    {
        int nexttime=Integer.MAX_VALUE;
        Process nextprocess=new Process();
        for(Process p:list)
        {
            if(((int)Math.floor(current_time/p.period)*p.period+p.period) < nexttime)
            {
                nexttime=(int)Math.floor(current_time/p.period)*p.period+p.period;
                nextprocess.deadline=nexttime+p.period;
                nextprocess.execution=p.execution;
                nextprocess.id=p.id;
            }
        }
        if(add) pq.add(nextprocess);
        return nexttime;
    }
    static int calculateFrequency()
    {
        double f=0.0;
        for(Process p:list)
        {
            f+=1.0*p.execution/p.period;
        }
        System.out.println("Utilization is "+f);
        f*=4;
        int x= Arrays.binarySearch(availablefreq, (int)Math.ceil(f));
        if(x<0)
            return availablefreq[-x-1];
        else
            return availablefreq[x];

    }
}



