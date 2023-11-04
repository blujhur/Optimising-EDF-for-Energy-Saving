import java.util.*;
class Task
{
    int id;
    int idInCore;
    int execution;
    int period;
    int deadline;
    Task()
    {

    }
    Task(int id, int idInCore, int execution, int period, int deadline)
    {
        this.id = id;
        this.idInCore = idInCore;
        this.execution = execution;
        this.period = period;
        this.deadline = deadline;
    }
}
class Job
{
    int execution;
    int deadline;
    int task_id;
    int idInCore;
    Job()
    {

    }
    Job(int execution, int deadline, int task_id, int idInCore)
    {
        this.execution = execution;
        this.deadline = deadline;
        this.task_id = task_id;
        this.idInCore = idInCore;
    }
}
public class EDFWithMigration {
    static ArrayList<Task> taskList = new ArrayList<>();
    static ArrayList<ArrayList<Integer>> taskAllocation = new ArrayList<>();
    static ArrayList<ArrayList<Job>> toTake = new ArrayList<>();
    static boolean shutDownable[];
    static int threshold[];
    static int availablefreq[]={2,3,4};
    static int availablevolt[]={1,3,4};
    static int numberOfCores = 4;
    static int slackTime[];
    public static void main(String[] args) {
        shutDownable = new boolean[numberOfCores];
        slackTime = new int[numberOfCores];
        threshold = new int[numberOfCores];
        for(int i=0;i<numberOfCores;i++)
        {
            toTake.add(new ArrayList<>());
        }
        initTasks();
        taskAllocate();
        initIsShutDownable();
        for(int i=0; i<numberOfCores; i++)
        {
            new Core(i,taskAllocation.get(i)).start();
        }
    }

    public static void initTasks()
    {
        Scanner sc = new Scanner(System.in);
        int n = sc.nextInt();
        for(int i=0;i<n;i++)
        {
            Task t = new Task();
            t.id = i;
            t.execution = sc.nextInt();
            t.period = sc.nextInt();
            t.deadline = t.period;
            taskList.add(t);
        }
        sc.close();       
    }
    public static void taskAllocate()
    {
        for(int i=0;i<numberOfCores;i++)
            taskAllocation.add(new ArrayList<Integer>());
        for(int i=0;i<taskList.size();i++)
        {
            taskAllocation.get(i%numberOfCores).add(i);
            //temporarily allocating round robin
            //more complex algorithm will be implemented later
        }
    }
    public static void initIsShutDownable()
    {
        for(int i=0;i<numberOfCores;i++)
        {
            if(checkIfShutDownable(taskAllocation.get(i), i))
                shutDownable[i] = true;
            else
                shutDownable[i] = false;
        }
    }
    public static boolean checkIfShutDownable(ArrayList<Integer> taskSet, int i)
    {
        boolean ans = false;
        int min = Integer.MAX_VALUE;
        for(int j=0;j<taskSet.size();j++)
        {
            int index = taskSet.get(j);
            min = Math.min(min, taskList.get(index).period - taskList.get(index).execution);
        }
        //algorithm to check shutdownability
        slackTime[i] = 2*min; //temporarily
        return slackTime[i] > threshold[i];
    }

    static class Core extends Thread {

        private int coreNumber;
        PriorityQueue<Job> pq;
        ArrayList<Job> list;
        ArrayList<Integer> utilcal;
        ArrayList<Integer> taskSet;
        ArrayList<Task> tasks;
        int numberOfTasks;
        double current_time = 0.0;
        public Core(int coreNumber, ArrayList<Integer> taskSet) {
            numberOfTasks = taskSet.size();
            this.coreNumber = coreNumber;
            pq=new PriorityQueue<>(new Comparator<Job>(){
                public int compare(Job p1, Job p2)
                {
                    return (int)(p1.deadline-p2.deadline);
                }
            });
            this.taskSet = new ArrayList<>(taskSet);
            list=new ArrayList<>();
            utilcal=new ArrayList<>(numberOfTasks);
        }

        @Override
        public void run() {
            init();
            while(current_time < 100)
            {
                if(!pq.isEmpty())
                {
                    Job j = pq.poll();
                    Random rand = new Random();
                    double f = 0.0;
                    utilcal.set(j.idInCore, j.execution);
                    int index = calculateFrequency();
                    f = availablefreq[index]/4.0;
                    int ac = rand.nextInt(j.execution)+1;
                    slackTime[coreNumber] += j.execution/f - ac/f;
                    double decision_point = Math.min(findtime(current_time,false), current_time+ ac/f);
                    
                    if(current_time+ac/f == decision_point)
                    {
                        System.out.println("Process " + j.idInCore + " is executing from " + current_time + " to " + (current_time+ac/f));
                        current_time += ac/f;
                        utilcal.set(j.idInCore, ac);
                        index = calculateFrequency();
                        f = availablefreq[index]/4.0;
                        slackTime[coreNumber] += j.execution/f - ac/f;
                       // time+=p.execution;
                    }
                    else
                    {
                        findtime(current_time, true);
                        System.out.println("Process "+ j.idInCore + " is executing from " + current_time + " to " + decision_point);
                        j.execution -= decision_point-current_time;
                        current_time = decision_point;
                        pq.add(j);
                    }
                }
                else
                {
                    if(shutDownable[coreNumber]) current_time = migrate(current_time);
                    else
                    {
                        double nexttask = findtime(current_time,true);
                        System.out.println("Idle from "+current_time+" to "+nexttask);
                        System.out.println();
                        current_time = nexttask;
                    }
                }
                //migrating my longest execution job to another core
                if(shutDownable[coreNumber])
                {
                    ArrayList<Job> temp = new ArrayList<>();
                    for(Job j: pq)
                        temp.add(j);
                    Collections.sort(temp, new Comparator<Job>(){
                        public int compare(Job p1, Job p2)
                        {
                            return p2.execution - p1.execution;
                        }
                    });
                    for(int i=0;i<temp.size();i++)
                    {
                        Job j = temp.get(i);
                        if(j.execution > slackTime[coreNumber])
                        {
                            pq.remove(j);
                            int newCore = coreNumber;
                            toTake.get(newCore).add(j);
                            
                            break;
                        }
                    }
                    addJobsFromToTake();
                    //for(int i)
                }
            }
        }
        public void init()
        {
            for(int j=0;j<numberOfTasks;j++)
            {
                int i=taskSet.get(j);
                Task t = new Task();
                t.id = taskList.get(i).id;
                t.execution = taskList.get(i).execution;
                t.period = taskList.get(i).period;
                t.deadline = taskList.get(i).deadline;
                t.idInCore = j;
                tasks.add(t);
            }
            for(int i=0;i<numberOfTasks;i++) utilcal.add(0);
            for(Task t:tasks)
            {
                Job j = new Job();
                j.execution = t.execution;
                j.deadline = t.deadline;
                j.task_id = t.id;
                j.idInCore = t.idInCore;
                pq.add(j);
                utilcal.set(t.idInCore, t.execution);
            }
        }
        public int calculateFrequency()
        {
            double f = 0.0;
            for(int i = 0; i < numberOfTasks ; i++)
            {
                f += 1.0*utilcal.get(i)/tasks.get(i).period;
            }
            
            System.out.println("Utilization is "+f);
            f *= 4;
            int x = Arrays.binarySearch(availablefreq, (int)Math.ceil(f));
            if(x < 0)
                x = -x-1;
            return x;

        }
        public double findtime(double current_time, boolean add)
        {
            int toadd[]=new int[list.size()];
            int nexttime=Integer.MAX_VALUE;
            Job nextprocess=new Job();
            for(Task p:tasks)
            {
                if(((int)Math.floor(current_time/p.period)*p.period+p.period) < nexttime)
                {
                    Arrays.fill(toadd, 0);
                    toadd[p.idInCore]=1;
                    nexttime=(int)Math.floor(current_time/p.period)*p.period+p.period;
                    nextprocess.deadline=nexttime+p.period;
                    nextprocess.execution=p.execution;
                    nextprocess.idInCore=p.idInCore;
                }
                else if(((int)Math.floor(current_time/p.period)*p.period+p.period) == nexttime)
                {
                    toadd[p.idInCore]=1;
                }
            }
            if(add) 
            {
                for(int i=0;i<numberOfTasks;i++)
                {
                    if(toadd[i]==1)
                    {
                        Job toAdd=new Job();
                        toAdd.idInCore = tasks.get(i).idInCore;
                        toAdd.execution = tasks.get(i).execution;
                        toAdd.deadline = nexttime+tasks.get(i).period;
                        pq.add(toAdd);
                    }
                }
            }
            return nexttime;
        }
        public void addJobsFromToTake()
        {
            for(Job j: toTake.get(coreNumber))
            {
                j.idInCore = -1;
                pq.add(j);
            }
            toTake.get(coreNumber).clear();
        }
        public void reinit(double timebefore, double timeafter)
        {
            //System.out.println("blah" + timebefore+" "+timeafter);
            for(Task p:tasks)
            {
                if(((int)Math.floor(timeafter/p.period) - (int)Math.floor(timebefore/p.period ))>=1)
                {
                    Job toadd=new Job();
                    toadd.idInCore = p.idInCore;
                    toadd.execution = p.execution;
                    toadd.deadline = (int)Math.floor(timeafter/p.period)*p.period + p.period;
                    pq.add(toadd);
                }
            }
        }
        public int migrate(double current_time)
        {
            boolean first = true;
            int idInCore_earliest = -1;
            int min = Integer.MAX_VALUE;
            for(Task t:tasks)
            {
                if((int)Math.floor(current_time/t.deadline)*t.deadline+t.deadline < min)
                {
                    min = (int)Math.floor(current_time/t.deadline)*t.deadline+t.deadline;
                    idInCore_earliest = t.idInCore;
                }
            }
            if(tasks.get(idInCore_earliest).execution - current_time > threshold[coreNumber])
            {
                for(int i = 0; i < numberOfCores; i++)
                {
                    if(i != coreNumber && !shutDownable[i] && (slackTime[i] > tasks.get(idInCore_earliest).execution))
                    {
                        Job j = new Job();
                        j.execution = tasks.get(idInCore_earliest).execution;
                        j.deadline = min;
                        j.idInCore = -1;
                        toTake.get(i).add(j);
                        slackTime[i] -= tasks.get(idInCore_earliest).execution;
                        migrate(min - tasks.get(idInCore_earliest).period + tasks.get(idInCore_earliest).execution);
                        break;
                        
                    }
                }
            }
            reinit(current_time, tasks.get(idInCore_earliest).deadline);
            return min - tasks.get(idInCore_earliest).period + tasks.get(idInCore_earliest).execution;
        }
    }
}
