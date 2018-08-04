
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Label;
import java.awt.Toolkit;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.text.DateFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Locale;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JFrame;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


/**
 * 
 */

/**
 * @author James
 *
 */
public class Spider {
	private ArrayList<String> aL = new ArrayList<String>();
	private static final int MAX_ITERATIONS = 30000;
	private LinkedHashMap<String,LinkedHashSet<String>> visitedUrls = new LinkedHashMap<String,LinkedHashSet<String>>();
	private static Queue<String> urlQueue = new LinkedList<String>();
	private String searchDate;
	PriorityQueue<String> pq = new PriorityQueue<String>(new DateComparator());
	/**
	 * 
	 */
	public Spider(String searchAfterDate) {
		searchDate = searchAfterDate;
		// TODO Auto-generated constructor stub
		startSearch("http://media.lcs.uwa.edu.au/echocontent/");
		//System.out.println(searchDate);
		/**
		try (Writer writer = new BufferedWriter(new OutputStreamWriter(
	              new FileOutputStream("List.txt"), "utf-8"))) {
			for (String string : aL) {
				writer.write(string);
			}
			String wD = pq.poll();
			if (wD != null) {
				//System.out.println(wD);
				writer.write(wD);
			}
				


		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("ERROR");
		}
		**/
		try(FileWriter fw = new FileWriter("List.txt", true);
			    BufferedWriter bw = new BufferedWriter(fw);
			    PrintWriter out = new PrintWriter(bw))
			{
				for (String string : aL) {
					out.write(string);
				}
				String wD = pq.poll();
				if (wD != null) {
					//System.out.println(wD);
					out.write(wD);
				}
			} catch (IOException e) {
			    //exception handling left as an exercise for the reader
			}
	}
	
	public void startSearch(String startingUrl) {
		urlQueue.add(startingUrl);
		
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		JFrame loading = new JFrame();
		loading.setUndecorated(true);
	    loading.setBounds(screenSize.width/2-250,screenSize.height/2-75,500,150);
	    loading.setLayout(new GridBagLayout());
	    GridBagConstraints c = new GridBagConstraints();
	    loading.getContentPane().setBackground(Color.DARK_GRAY);
	    Label l = new Label("Retrieving Lectures", 1);
	    c.fill = GridBagConstraints.BOTH; 
		c.gridx = 0;
		c.gridy = 0;
		c.anchor = GridBagConstraints.PAGE_START;
		loading.add(l, c);
	    Label l2 = new Label("Retrieving Lectures", 0);
	    c.fill = GridBagConstraints.BOTH; 
	    c.weightx = 1.0;
		c.gridx = 0;
		c.gridy = 1;
		c.anchor = GridBagConstraints.PAGE_END;
		loading.add(l2, c);
		l.setBackground(Color.DARK_GRAY);
		l.setForeground(Color.WHITE);
		l2.setBackground(Color.DARK_GRAY);
		l2.setForeground(Color.WHITE);
		loading.setVisible(true);
		for (int i = 0; i < MAX_ITERATIONS; i++) {
			
			String urlToSearch = nextURL();
			l2.setText("Searching: " + urlToSearch);
			
			if (urlToSearch == null) break;
			new SpiderLeg(urlToSearch);
			
			
			
		}
		loading.dispose();
	}
	
	public class DateComparator implements Comparator<String>
	{
	    @Override
	    public int compare(String x, String y)
	    {
			Locale locale = new Locale("en", "US");
			DateFormatSymbols dateFormatSymbols = new DateFormatSymbols(locale);
			dateFormatSymbols.setShortMonths(new String[]{
			        "Jan",
			        "Feb",
			        "Mar",
			        "Apr",
			        "May",
			        "Jun",
			        "Jul",
			        "Aug",
			        "Sep",
			        "Oct",
			        "Nov",
			        "Dec"
			});
			SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy hh:mm", dateFormatSymbols);
			
			try {
				return sdf.parse(y).compareTo(sdf.parse(x));
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    	
	        return 0;
	    }
	}

	private String nextURL() {
		return urlQueue.poll();
		// TODO Auto-generated method stub
		
	}
	

	public class SpiderLeg {
		String urlToSearch;
		public SpiderLeg(String urlToSearch) {
			// TODO Auto-generated constructor stub
			//urlToSearch = "https://media.lcs.uwa.edu.au/echocontent/1831/2/f9fd94bf-7086-46f2-a29c-5ac73adf4e74/";
			this.urlToSearch = urlToSearch;
			try {
				
				Document doc = Jsoup.connect(urlToSearch).get();
				Elements lectureInfo = doc.select("a[href*=presentation.xml]");
				if (lectureInfo.size() == 1) {
					String presentationUrl = urlToSearch + "presentation.xml";
					Document xmlDoc = Jsoup.connect(presentationUrl).get();
					String unitInfo = findLectureInformation(xmlDoc);
					String fullUnitInfo = null;
					Elements video = doc.select("a[href*=audio-vga.m4v]");
					if (video.size() == 1) {
						fullUnitInfo = urlToSearch+"audio-vga.m4v" + "|" + unitInfo + "+";
						aL.add(fullUnitInfo);
					}

					
				} else {
					findURLS(doc);
				}

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println("ERROR");
			}
		}
		

		
		private void findURLS(Document doc) {
			Locale locale = new Locale("en", "US");
			DateFormatSymbols dateFormatSymbols = new DateFormatSymbols(locale);
			dateFormatSymbols.setShortMonths(new String[]{
			        "Jan",
			        "Feb",
			        "Mar",
			        "Apr",
			        "May",
			        "Jun",
			        "Jul",
			        "Aug",
			        "Sep",
			        "Oct",
			        "Nov",
			        "Dec"
			});
			// TODO Auto-generated method stub
			Elements urls = doc.select("a[href]");

			for (Element element : urls) {
				String url = element.text();
				DateComparator dateC = new DateComparator();
				if (url.substring(0, url.length()-1).matches("\\d+") && Integer.parseInt(url.substring(0, url.length()-1)) > 1826) {

						addURL(urlToSearch+url, element.parent().nextElementSibling().text());
					
				}
				
				if ((url.substring(0, url.length()-1).matches("\\d+")) && Integer.parseInt(url.substring(0, url.length()-1)) < 300) {
					addURL(urlToSearch+url, element.parent().nextElementSibling().text());
				}
				
				if ((url.substring(0, url.length()-1).matches("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}"))) {
					if (searchDate == null || (dateC.compare(searchDate, element.parent().nextElementSibling().text())) > 0) {
						addURL(urlToSearch+url, element.parent().nextElementSibling().text());
					}
				}

			}
		}
		
		private void addURL(String url, String date) {
			if (!visitedUrls.containsKey(url)) {
				visitedUrls.put(url, new LinkedHashSet<String>());
				urlQueue.add(url);
				pq.add(date);

			}
		}

		private String findLectureInformation(Document xmlDoc) throws IOException {
			String desc = xmlDoc.select("description").get(0).text();
			String timestamp = xmlDoc.select("start-timestamp").get(0).text();
			String name = xmlDoc.select("name").get(0).text();
			String unitCode = findUnitCode(name, desc);
			String date = findDate(desc, timestamp);
			if (date == null || unitCode == null) {
				
			} else {

			}
			return unitCode+"|"+date;
		}
		
		private String findUnitCode(String unParsedName, String unParsedDesc) throws IOException {

			String unitCode = findMatching("\\w{4}\\d{4}", unParsedName);
			if (unitCode == null) {
				unitCode = findMatching("\\w{4}\\d{4}", unParsedDesc);
			}
			if (unitCode == null && unParsedName.length() != 0) {
				unitCode = unParsedName;
			} else if (unitCode == null && unParsedDesc.length() != 0) {
				unitCode = unParsedDesc;
			} 
			return unitCode;
		}
		
		
		private String findDate(String unParsedDesc, String timestamp) {
			/**
			String date;
			String dayMonth = findMatching("\\d{2}/\\d{2}", unParsedDesc);
			if(dayMonth==null) {
				dayMonth = "??/??";
			}
			**/
			
			String date = findMatching("\\d{2}/\\d{2}/\\d{4}", timestamp);
			if (date == null) date = "??/??/????";

			return timestamp;
		}
		
		private String findMatching(String regex, String toFindIn) {
			Pattern pattern = Pattern.compile(regex);			
			Matcher matcher = pattern.matcher(toFindIn);
			
			if (matcher.find()) {
				return matcher.group(0);
			} else {
				return null;
			}
			
		
		}
		
	}


}
