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
class Core
{
    
}
class ccedf
{
    static int availablefreq[]={2,3,4};
    static int availablevolt[]={1,3,4};
    static ArrayList<Process> list=new ArrayList<>();
    static PriorityQueue<Process> pq=new PriorityQueue<>(new Comparator<Process>(){
        public int compare(Process p1, Process p2)
        {
            return p1.deadline-p2.deadline;
        }
    });
    static ArrayList<Integer> utilcal;
    public static void main(String[] args)
    {
        //energy calculation for EDF
        init();
        
    }
    public static void exec(PriorityQueue<Process> pq, ArrayList<Process> list)
    {
        Random rand=new Random();
        //int index=calculateFrequency();
        double f=0.0;
        //System.out.println("Frequency is "+f);
        double current_time=0;
        double busytime=0;
        double idletime=0;
        double energy=0.0;
        Scanner sc=new Scanner(System.in);
        int co=1;
        while(co<7)
        {
            if(!pq.isEmpty())
            {
                Process p=pq.poll();
                //System.out.println("Actual Time");
                //System.out.println("blah" + utilcal+" "+p.execution);
                utilcal.set(p.id-1, p.execution);
                int index=calculateFrequency();
                f=availablefreq[index]/4.0;
                int ac=rand.nextInt(p.execution)+1;
                double decision_point=Math.min(findtime(current_time,false),current_time+p.execution);
                if(current_time+ac/f==decision_point)
                {
                    System.out.println("Process "+p.id+" is executing from "+current_time+" to "+(current_time+ac/f));
                    current_time+=ac/f;
                    utilcal.set(p.id-1, ac);
                    index=calculateFrequency();
                    f=availablefreq[index]/4.0;

                   // time+=p.execution;
                }
                else
                {
                    findtime(current_time, true);
                    System.out.println("Process "+p.id+" is executing from "+current_time+" to "+decision_point);
                    p.execution-=decision_point-current_time;
                    current_time=decision_point;
                    // time+=decision_point-current_time;
                    pq.add(p);
                }
                energy+=ac/f*availablevolt[index]*availablevolt[index]*f;

                utilcal.set(p.id-1, ac);
                // reinit(current_time, current_time+ac/f);
                current_time += ac/f;
                
                // System.out.println("blah" + utilcal+" "+ac);
                System.out.println();
                busytime +=ac/f;
                
                
            }
            else
            {
                double nexttask=findtime(current_time,true);
                System.out.println("Idle from "+current_time+" to "+nexttask);
                System.out.println();
                idletime+=nexttask-current_time;
                energy+=(nexttask-current_time)*availablevolt[0]*availablevolt[0]*availablefreq[0]/4.0;
                current_time=nexttask;
            }
        }
        System.out.println("Busy time is "+busytime);
        System.out.println("Idle time is "+idletime);
        System.out.println("Energy is "+energy);
        sc.close();
    }
    static void init()
    {
        Scanner sc=new Scanner(System.in);
        int n=sc.nextInt();
        utilcal=new ArrayList<>(n);
        //System.out.println(utilcal);
        for(int i=0;i<n;i++) utilcal.add(0);
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
        for(Process p:list)
            utilcal.set(p.id-1,p.execution);
        
        sc.close();
    }
    static void reinit(double timebefore, double timeafter)
    {
        //System.out.println("blah" + timebefore+" "+timeafter);
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
    static double findtime(double current_time, boolean add)
    {
        int toadd[]=new int[list.size()];
        double nexttime=Integer.MAX_VALUE;
        Process nextprocess=new Process();
        for(Process p:list)
        {
            if(((current_time/p.period)*p.period+p.period) < nexttime)
            {
                Arrays.fill(toadd, 0);
                toadd[p.id-1]=1;
                nexttime=(current_time/p.period)*p.period+p.period;
                nextprocess.deadline=nexttime+p.period;
                nextprocess.execution=p.execution;
                nextprocess.id=p.id;
            }
            else if(((current_time/p.period)*p.period+p.period) == nexttime)
            {
                toadd[p.id-1]=1;
            }
        }
        if(add) 
        {
            for(int i=0;i<list.size();i++)
            {
                if(toadd[i]==1)
                {
                    Process toaddprocess=new Process(list.get(i).id, list.get(i).execution, list.get(i).period);
                    toaddprocess.deadline=nexttime+toaddprocess.period;
                    pq.add(toaddprocess);
                }
            }
        }
        return nexttime;
    }
    static int calculateFrequency()
    {
        double f=0.0;
        for(int i=0;i<list.size();i++)
        {
            f+=1.0*utilcal.get(i)/list.get(i).period;
        }
        
        System.out.println("Utilization is "+f);
        f*=4;
        int x= Arrays.binarySearch(availablefreq, (int)Math.ceil(f));
        if(x<0)
            x=-x-1;
        return x;

    }
    
}



