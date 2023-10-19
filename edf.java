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
                // System.out.println("Current time is "+current_time);
                // for(Process x:pq)
                //     System.out.println(x.id+" "+x.deadline);
                // System.out.println();
                // System.out.println();
                Process p=pq.poll();
                int decision_point=Math.min(findtime(current_time,false),current_time+p.execution);
                if(current_time+p.execution==decision_point)
                {
                    System.out.println("Process "+p.id+" is executing from "+current_time+" to "+(current_time+p.execution));
                    current_time+=p.execution;
                    time+=p.execution;
                }
                else
                {
                    findtime(current_time, true);
                    System.out.println("Process "+p.id+" is executing from "+current_time+" to "+decision_point);
                    p.execution-=decision_point-current_time;
                    current_time=decision_point;
                    time+=decision_point-current_time;
                    pq.add(p);
                }
                //System.out.println("Process "+p.id+" is executing from "+current_time+" to "+(current_time+p.execution));
                //reinit(current_time, current_time+p.execution);
                //for(Process x:pq)
                    //System.out.println(x.id+" "+x.deadline);
                //
                // current_time+=p.execution;
                // time+=p.execution;
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
        int toadd[]=new int[list.size()];
        int nexttime=Integer.MAX_VALUE;
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
}



