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
class edf
{
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
        int current_time=0;
        int time=0;
        int counter=0;
        while(counter<6)
        {
            if(!pq.isEmpty())
            {
                Process p=pq.poll();
                System.out.println("Process "+p.id+" is executing from "+current_time+" to "+(current_time+p.execution));
                reinit(current_time, current_time+p.execution);
                //for(Process x:pq)
                    //System.out.println(x.id+" "+x.deadline);
                //System.out.println();
                current_time+=p.execution;
                time+=p.execution;
                counter++;
            }
            else
            {
                int nexttask=findtime(current_time,true);
                System.out.println("Idle from "+current_time+" to "+nexttask);
                time+=nexttask-current_time;
                current_time=nexttask;
            }
        }
        System.out.println("Energy is "+time*4*4);
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
    static void reinit(int timebefore, int timeafter)
    {
        for(Process p:list)
        {
            if((timeafter/p.period - timebefore/p.period )>=1)
            {
                Process toadd=new Process(p.id, p.execution, p.period);
                toadd.deadline=timeafter/p.period*toadd.period + toadd.period;
                pq.add(toadd);
            }
        }
    }
    static int findtime(int current_time, boolean add)
    {
        int nexttime=Integer.MAX_VALUE;
        Process nextprocess=new Process();
        for(Process p:list)
        {
            if(((current_time/p.period)*p.period+p.period) < nexttime)
            {
                nexttime=(current_time/p.period)*p.period+p.period;
                nextprocess.deadline=nexttime+p.period;
                nextprocess.execution=p.execution;
                nextprocess.id=p.id;
            }
        }
        if(add) pq.add(nextprocess);
        return nexttime;
    }
}



