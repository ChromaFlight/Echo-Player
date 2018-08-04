import java.awt.*;        // Using AWT container and component classes
import java.awt.event.*;  // Using AWT event classes and listener interfaces
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.sun.jna.NativeLibrary;

import uk.co.caprica.vlcj.component.EmbeddedMediaPlayerComponent;
import uk.co.caprica.vlcj.player.MediaPlayer;
import uk.co.caprica.vlcj.player.MediaPlayerEventAdapter;

import com.sun.java.swing.*;


// An AWT GUI program inherits from the top-level container java.awt.Frame
public class Gui extends Frame  {
	
	private final EmbeddedMediaPlayerComponent mediaPlayerComponent;
	private static File vlcInstallPath = new File("C:/Program Files/VideoLAN/VLC");
	// Constructor to setup the GUI components and event handlers
	private String dateToSearchAfter = null;
	
	
	public class LectureC {
		

		Date date;
		String dateString;
		String url;
		String unitCode;
		public LectureC(String s) {
			
			String sSplit[] = s.split("\\|");
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
			unitCode = sSplit[1];
			url = sSplit[0];
			try {
				date = sdf.parse(sSplit[2]);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			dateString = sSplit[2];
		}
		public Date getDate() {
			return date;
		}
		public String getUrl() {
			return url;
		}
		public String getUnitCode() {
			return unitCode;
		}
		public String getDateString() {
			return dateString;
		}
		
		
	}
	private ArrayList<LectureC> lecArrayList = new ArrayList<LectureC>();
	private LinkedHashSet<String> filteredList = new LinkedHashSet<String>();
	private ArrayList<String> aL = new ArrayList<String>();
	public Gui() {
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		if (!readFile()) {
			new Spider(null);
		}
		
		if (!readFile()) {
			getToolkit().beep();
		}
		for (String string : aL) {
			lecArrayList.add(new LectureC(string));
			
		}
		
		
		
		JFrame f = new JFrame();
		JPanel p = new JPanel();
		f.add(p);
		f.setExtendedState(f.getExtendedState() | JFrame.MAXIMIZED_BOTH);
		p.setVisible(false);

	    f.setBounds(0,0,screenSize.width, screenSize.height);
	    p.setBounds(100,100,f.getBounds().width*2/3,f.getBounds().height*2/3-40);
	    p.setLayout(new GridBagLayout());
	    GridBagConstraints c = new GridBagConstraints();
		f.setLayout(new BorderLayout());
		

		
		JTextField userText = new JTextField(1);
		userText.setBorder(BorderFactory.createMatteBorder(
                1, 1, 1, 1, Color.GRAY));
		c.fill = GridBagConstraints.HORIZONTAL;    //make this component tall
		c.weightx = 1.0;
		c.weighty = 0.8;
		c.gridwidth = 1;
		c.gridx = 0;
		c.gridy = 1;
		c.anchor = GridBagConstraints.PAGE_START;
		c.insets = new Insets(10,0,0,0);
		p.add(userText, c);
		c.fill = GridBagConstraints.HORIZONTAL;    //make this component tall
		c.weightx = 1.0;
		c.weighty = 0.0;
		c.gridwidth = 1;
		c.gridx = 0;
		c.gridy = 0;
		c.anchor = GridBagConstraints.PAGE_START;
		p.add(new Label("Filter"), c);
		c.fill = GridBagConstraints.HORIZONTAL;    //make this component tall
		c.weightx = 0.0;
		c.weighty = 0.0;
		c.gridwidth = 1;
		c.gridx = 1;
		c.gridy = 1;
		Button refresh = new Button("Refresh");
		refresh.setBounds(new Rectangle(100, 100, 100, 100));
		p.add(refresh, c);
		ActionListener refreshButtonHandler = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (mediaPlayerComponent.getMediaPlayer().isPlaying()) {
					mediaPlayerComponent.getMediaPlayer().pause();
				}
				f.setVisible(false);
				
				new Spider(dateToSearchAfter);
				readFile();
				lecArrayList.clear();
				for (String string : aL) {
					lecArrayList.add(new LectureC(string));
					
				}
				f.setVisible(true);
			}
		};
		refresh.addActionListener(refreshButtonHandler);
		DefaultListModel<String> listModel = new DefaultListModel<String>();
		JList<String> lectureList = new JList<String>(listModel);
		JScrollPane sp = new JScrollPane(lectureList);
		lectureList.setVisibleRowCount(10);
		lectureList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		lectureList.setBorder(BorderFactory.createMatteBorder(
                1, 1, 1, 1, Color.GRAY));
		c.fill = GridBagConstraints.BOTH;
		c.anchor = GridBagConstraints.PAGE_START;
		c.ipady = 500;
		c.weighty = 1.0;
		c.weightx = 0.5;
		c.gridwidth = 1;
		c.gridx = 0;
		c.gridy = 2;
		p.add(sp, c);
		DefaultListModel<String> listModel2 = new DefaultListModel<String>();
		JList<String> lectureList2 = new JList<String>(listModel2);
		JScrollPane sp2 = new JScrollPane(lectureList2);
		lectureList2.setVisibleRowCount(10);
		lectureList2.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		lectureList2.setBorder(BorderFactory.createMatteBorder(
                1, 1, 1, 1, Color.GRAY));
		c.fill = GridBagConstraints.BOTH;
		c.anchor = GridBagConstraints.PAGE_START;  
		c.weightx = 0.5;
		c.gridx = 1;
		c.gridy = 2;
		p.add(sp2, c);
		

		
		for (int i = 0; i < lecArrayList.size(); i++) {
			if (!filteredList.contains(lecArrayList.get(i).getUnitCode())) {
				filteredList.add(lecArrayList.get(i).getUnitCode());
				listModel.addElement(lecArrayList.get(i).getUnitCode());
			}
		}
		ArrayList<Integer> filteredDates = new ArrayList<Integer>();
		
		
		f.setTitle("Echo Player");  // "super" Frame sets title
		ListSelectionListener lectureHandler = new ListSelectionListener() {
			

			@Override
			public void valueChanged(ListSelectionEvent e) {
				// TODO Auto-generated method stub
				if(!e.getValueIsAdjusting()){
					String unitCode = lectureList.getSelectedValue();
					listModel2.removeAllElements();
					filteredDates.clear();
					int count = 0;
					for (int i = 0; i < lecArrayList.size(); i++) {
						if (lecArrayList.get(i).getUnitCode().equals(unitCode)) {
							filteredDates.add(i);
							listModel2.addElement(lecArrayList.get(i).getDateString()+  " Lecture: "+ ++count);
						}
					}
					lectureList.setVisible(true);
				}
			}
		};
		
		lectureList.addListSelectionListener(lectureHandler);
		ListSelectionListener lectureHandler2 = new ListSelectionListener() {
			
			@Override
			public void valueChanged(ListSelectionEvent e) {
				if(!e.getValueIsAdjusting()){
					Integer index = lectureList2.getSelectedIndex();
					if (index != -1) {
						LectureC lecture = lecArrayList.get(filteredDates.get(index));
						mediaPlayerComponent.getMediaPlayer().playMedia(lecture.getUrl());
						p.setVisible(false);
					}
				}
			}
		};
		lectureList2.addListSelectionListener(lectureHandler2);
		NativeLibrary.addSearchPath("libvlc", vlcInstallPath.getAbsolutePath());
		mediaPlayerComponent = new EmbeddedMediaPlayerComponent() {
		    @Override
		    public void mouseClicked(MouseEvent e) {
		    	p.setVisible(false);
		    }

		    @Override
		    public void mouseWheelMoved(MouseWheelEvent e) {
		    }

		    @Override
		    public void keyPressed(KeyEvent e) {
		    	if (e.isControlDown()) {
		    		p.setVisible(!p.isVisible());
		    	} else if (e.getKeyCode() == KeyEvent.VK_SPACE) {
		    		mediaPlayerComponent.getMediaPlayer().pause();
		    	}
		    	
		    	switch (e.getKeyCode()) {
			    	case KeyEvent.VK_RIGHT: {
			    		mediaPlayerComponent.getMediaPlayer().skip(5000);
			    	} break;
			    	case KeyEvent.VK_LEFT: {
			    		mediaPlayerComponent.getMediaPlayer().skip(-5000);
			    	} break;
			    	case KeyEvent.VK_UP: {
			    		mediaPlayerComponent.getMediaPlayer().setVolume(mediaPlayerComponent.getMediaPlayer().getVolume()+2);
			    	} break;
			    	case KeyEvent.VK_DOWN: {
			    		mediaPlayerComponent.getMediaPlayer().setVolume(mediaPlayerComponent.getMediaPlayer().getVolume()-2);
			    	} break;
		    	}
		    }
		    
		};
		
		f.addWindowListener(new WindowAdapter() {
	        @Override
	        public void windowClosing(WindowEvent e) {
	            mediaPlayerComponent.release();
	            System.exit(0);
	        }
	        
		});
		JSlider slider = new JSlider(JSlider.HORIZONTAL,
                0, 0, 0);
		
		JSlider slider2 = new JSlider(JSlider.HORIZONTAL,
                0, 40, 10);
		JSlider slider3 = new JSlider(JSlider.HORIZONTAL,
                0, 200, 100);
		JPanel p2 = new JPanel();
		f.add(p2, BorderLayout.PAGE_END);
		slider.addChangeListener(new ChangeListener() {
			
			@Override
			public void stateChanged(ChangeEvent e) {
				JSlider source = (JSlider)e.getSource();
				if (source.getValueIsAdjusting()) {
					mediaPlayerComponent.getMediaPlayer().setTime(slider.getValue());
				}
			}
		});
		
		slider2.addChangeListener(new ChangeListener() {
			
			@Override
			public void stateChanged(ChangeEvent e) {
				mediaPlayerComponent.getMediaPlayer().setRate(((float) slider2.getValue())/10.0f);
			}
		});
		slider3.addChangeListener(new ChangeListener() {
			
			@Override
			public void stateChanged(ChangeEvent e) {
				mediaPlayerComponent.getMediaPlayer().setVolume(slider3.getValue());
			}
		});
		p2.setLayout(new GridBagLayout());
		GridBagConstraints c2 = new GridBagConstraints();
		c2.anchor = GridBagConstraints.WEST;
		c2.fill = GridBagConstraints.BOTH;
        c2.weightx = 0.01;
        c2.weighty = 1.0;
        c2.gridx = 2;
        c2.gridy = 1;
		p2.add(slider2,c2);
		c2.anchor = GridBagConstraints.EAST;
		c2.fill = GridBagConstraints.BOTH;
        c2.weightx = 0.98;
        c2.weighty = 1.0;
        c2.gridx = 3;
        c2.gridy = 1;
		p2.add(slider,c2);
		c2.anchor = GridBagConstraints.EAST;
		c2.fill = GridBagConstraints.BOTH;
        c2.weightx = 0.01;
        c2.weighty = 1.0;
        c2.gridx = 1;
        c2.gridy = 1;
		p2.add(slider3,c2);
		
		c2.anchor = GridBagConstraints.EAST;
		c2.fill = GridBagConstraints.BOTH;
        c2.weightx = 0.01;
        c2.weighty = 1.0;
        c2.gridx = 1;
        c2.gridy = 0;
		p2.add(new Label("Volume", 1),c2);
		
		c2.anchor = GridBagConstraints.EAST;
		c2.fill = GridBagConstraints.BOTH;
        c2.weightx = 0.01;
        c2.weighty = 1.0;
        c2.gridx = 2; 
        c2.gridy = 0;
		p2.add(new Label("Speed", 1),c2);
		
		c2.anchor = GridBagConstraints.EAST;
		c2.fill = GridBagConstraints.BOTH;
        c2.weightx = 0.98;
        c2.weighty = 1.0;
        c2.gridx = 3;
        c2.gridy = 0;
		p2.add(new Label("Playback", 1),c2);
		JButton playButton = new JButton("Play");
		playButton.setBorder(BorderFactory.createMatteBorder(
                1, 1, 1, 1, Color.GRAY));
		ActionListener playButtonHandler = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				mediaPlayerComponent.getMediaPlayer().pause();
			}
		};
		playButton.addActionListener(playButtonHandler);
		c2.anchor = GridBagConstraints.EAST;
		c2.fill = GridBagConstraints.BOTH;
        c2.weightx = 0.01;
        c2.weighty = 1.0;
        c2.gridheight = 2;
        c2.gridx = 0;
        c2.gridy = 0;
        c2.insets = new Insets(5, 0, 5, 0);
        p2.add(playButton, c2);
		f.add(mediaPlayerComponent, BorderLayout.CENTER);
		mediaPlayerComponent.setVisible(true);
		f.setVisible(true);
		mediaPlayerComponent.getMediaPlayer().setRepeat(true);
		mediaPlayerComponent.getMediaPlayer().playMedia("nyan.webm");
		//slider.setMaximum((int) mediaPlayerComponent.getMediaPlayer().getLength());
		mediaPlayerComponent.getMediaPlayer().addMediaPlayerEventListener(new MediaPlayerEventAdapter() {
            /**
             *
             * @param mediaPlayer play video
             * @param newTime get every millisecond changed in the mediaplayer
             * when is in playing mode
             */
            @Override
            public void timeChanged(MediaPlayer mediaPlayer, long newTime) {
                super.timeChanged(mediaPlayer, newTime);
                // here put you code to set new slider progress value 
                	slider.setValue((int) newTime);
               }
            /**
            *
            * @param mediaPlayer play video
            * @param newTime get every millisecond changed in the mediaplayer
            * when is in playing mode
            */
           @Override
           public void lengthChanged(MediaPlayer mediaPlayer, long newTime) {
               super.lengthChanged(mediaPlayer, newTime);
               // here put you code to set new slider progress value 
       		slider.setMaximum((int) mediaPlayerComponent.getMediaPlayer().getLength());
               }
            });
		
	    userText.addKeyListener
	      (new KeyAdapter() {
	         public void keyPressed(KeyEvent e) {
	           int key = e.getKeyCode();

	           if (key == KeyEvent.VK_K && userText.getText().toLowerCase().equals("dar")) {
	        	   p.setBackground(Color.DARK_GRAY);
	        	   p.setForeground(Color.LIGHT_GRAY);
	        	   p2.setForeground(Color.LIGHT_GRAY);
	        	   p2.setBackground(Color.DARK_GRAY);
	        	   lectureList.setBackground(Color.DARK_GRAY);
	        	   lectureList.setForeground(Color.LIGHT_GRAY);
	        	   lectureList2.setForeground(Color.LIGHT_GRAY);
	        	   lectureList2.setBackground(Color.DARK_GRAY);
	        	   sp.getVerticalScrollBar().setBackground(Color.DARK_GRAY);
	        	   sp.getHorizontalScrollBar().setBackground(Color.DARK_GRAY);
	        	   sp.getHorizontalScrollBar().setBorder(BorderFactory.createMatteBorder(
	                       1, 1, 1, 1, Color.GRAY));
	        	   sp.getVerticalScrollBar().setBorder(BorderFactory.createMatteBorder(
	                       1, 1, 1, 1, Color.GRAY));
	        	   for (Component c :p.getComponents()) {
	        		   c.setBackground(Color.DARK_GRAY);
	        		   c.setForeground(Color.LIGHT_GRAY);
	        	   }
	        	   for (Component c :p2.getComponents()) {
	        		   c.setBackground(Color.DARK_GRAY);
	        		   c.setForeground(Color.LIGHT_GRAY);
	        	   }
	           } else if (key == KeyEvent.VK_ENTER) {
	        	   listModel.removeAllElements();
	        	   filteredList.clear();
	        	   for (LectureC l : lecArrayList) {
					if (l.unitCode.toLowerCase().contains(userText.getText().toLowerCase())) {
						if (!filteredList.contains(l.getUnitCode())) {
							filteredList.add(l.getUnitCode());
							listModel.addElement(l.getUnitCode());
						}

					}
	        	   }
	              
	              }
	           }
	         }
	      );
		
		

	}
	private boolean readFile() {
		aL  = new ArrayList<String>();
		File file = new File("List.txt");
		 
		BufferedReader br;
		try {
			br = new BufferedReader(new FileReader(file));
			String st;
			while ((st = br.readLine()) != null) {

				String s[] = st.split("\\+");


				for (String string : s) {
					if (string.startsWith("http")) {
					aL.add(string);
					} else {
						dateToSearchAfter = string;
					}
				}

			}
			br.close();
			return true;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			return false;
		}
		 

	}
	
}