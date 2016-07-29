import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

public class tester {

	public static void main(String args[]) throws Exception
	{
		Stack<timestamps> stack =new Stack<timestamps>();
		ArrayList<timestamps> list =new ArrayList<timestamps>();
		HashMap<String,timestamps> map=new HashMap<String,timestamps>(); 
		FileInputStream fis_lan = null;
		File file = new File("test_log.txt");
		fis_lan = new FileInputStream(file);
		// while((f2=fis_lan.getChannel().tryLock())==null){}
		BufferedReader br = new BufferedReader(new InputStreamReader(fis_lan));

		String currentline;
		
		String start_string="Critical Section execution started at";
		String end_string="Critical Section execution finished at";
		
		while ((currentline = br.readLine()) != null)
		{
			String[] split=currentline.split(" :: ");
			
			if(map.containsKey(split[0]+split[1]))
			{
				timestamps temp_time= map.get(split[0]+split[1]);
				if(split[2].equals(start_string))
				{
					temp_time.setStartTime(Long.parseLong(split[3]));
				}
				else if (split[2].equals(end_string))
				{
					temp_time.setEndTime(Long.parseLong(split[3]));
				}
				else
				{
					System.out.println("Wrong String");
				}
				
				map.put(split[0]+split[1], temp_time);
			}
			else
			{
				
				if(split[2].equals(start_string))
				{
					map.put(split[0]+split[1],new timestamps(Long.parseLong(split[3]),(long) -1));
				}
				else if (split[2].equals(end_string))
				{
					map.put(split[0]+split[1],new timestamps((long)-1,Long.parseLong(split[3])));
				}
				else
				{
					System.out.println("Wrong String");
				}
				
			}
			
			
			
		}
		
		br.close();
		
		
		
		
		/*
		for(timestamps t : list)
		{
			System.out.println("start : " + t.getStartTime() + "   Endtime : " + t.getEndTime());
		}
		
		for(int i=0;i<list.size();i++)
		{
			System.out.println("start : " + list.get(i).getStartTime() + "   Endtime : " + list.get(i).getEndTime());
			
		}
		*/
		
		
		
		for(Map.Entry<String,timestamps> mapElement : map.entrySet())
		{
			//System.out.println("Key : " + mapElement.getKey() + " Value_start : " + mapElement.getValue().getStartTime() + " Value_End : " + mapElement.getValue().getEndTime());
			stack.push(mapElement.getValue());
		}
		while(!stack.isEmpty())
		{
			timestamps t=stack.pop();
			//System.out.println("start : " + t.getStartTime() + "   Endtime : " + t.getEndTime());
			list.add(t);
		}
		
		
		
		stack.push(list.get(0));
		
		for(int i=1;i<list.size();i++)
		{
			
			if((list.get(i).getStartTime()<stack.peek().getEndTime()))
			{
				System.out.println("Protocol ran incorrectly");
				System.exit(1);
			}
			else
			{
				stack.push(list.get(i));
			}
			
		}
		System.out.println("Protocol ran Perfectly");
		

		
		
		
		
		
	}
	
	
}

class timestamps implements Comparable<timestamps>
{
	private Long startTime;
	private Long endTime;
	
	timestamps(Long startTime,Long i)
	{
		this.startTime=startTime;
		this.endTime=i;
	}
	
	public Long getStartTime() {
		return startTime;
	}

	public void setStartTime(Long startTime) {
		this.startTime = startTime;
	}

	public Long getEndTime() {
		return endTime;
	}

	public void setEndTime(Long endTime) {
		this.endTime = endTime;
	}

	@Override
	public int compareTo(timestamps o) {
		// TODO Auto-generated method stub
		if(this.startTime > o.startTime)
		{
			return -1;
		}
		else		
		return 1;
	}

	
}


