package com.jobscheduledb.controller;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import org.apache.ibatis.jdbc.RuntimeSqlException;
import org.apache.ibatis.jdbc.ScriptRunner;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import javaxt.io.Directory;
import javaxt.io.Directory.Event;

@RestController
public class JobScheduleController {
	static int va =0;
	Connection con;
	@Autowired
	private DataSource datasource;
	
	@RequestMapping("/home")

	public ModelAndView home()
	{
		return new ModelAndView("home");
	}
	@RequestMapping("/firstautomodetest")
	public void testsyncMethod()
	{
		Directory folder = new Directory("F:\\sqlfiles");
		Directory folderCopy = new Directory("F:\\sqlfilescopy");
		
		try {
			sync(folder, folderCopy);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static void sync(Directory source, Directory destination) throws Exception {
		JobScheduleController js = new JobScheduleController();
		  //Create an event que
		    java.util.List events = source.getEvents();
		 
		  //Process events
		    while (true){
		 
		        Event event;
		 
		      //Wait for new events to be added to the que
		        synchronized (events) {
		            while (events.isEmpty()) {
		              try {
		            	//  js.autoSchedule();
		            	  System.out.println("waiting to do a event");
		                  events.wait();
		                  System.out.println("events are waiting");
		              }
		              catch (InterruptedException e) {}
		            }
		            event = (Event) events.remove(0);
		        }
		 
		 
		      //Process event
		        int eventID = event.getEventID();
		        if (eventID==Event.DELETE){
		 
		          //Build path to the file in the destination directory
		            String path = destination +"\\"+ event.getFile().substring(source.toString().length());
		 System.out.println("path is "+path);
		          //Delete the file/directory
		            new java.io.File(path).delete();
		        }
		        else{
		 
		          //Check if the event is associated with a file or directory so
		          //we can use the JavaXT classes to create or modify the target.
		            java.io.File obj = new java.io.File(event.getFile());
		            if (obj.isDirectory()){
		                javaxt.io.Directory dir = new javaxt.io.Directory(obj);
		                javaxt.io.Directory dest = new javaxt.io.Directory(destination + dir.toString().substring(source.toString().length()));
		 
		                switch (eventID) {
		 
		                    case (Event.CREATE): dir.copyTo(dest, true);System.out.println("event creation"); break;
		                    case (Event.MODIFY):System.out.println("event modification"); break; //TODO
		                    case (Event.RENAME): {
		                        javaxt.io.Directory orgDir = new javaxt.io.Directory(event.getOriginalFile());
		                        dest = new javaxt.io.Directory(destination + orgDir.toString().substring(source.toString().length()));
		                        dest.rename(dir.getName());System.out.println("renaming");
		                        break;
		                    }
		                }
		 
		            }
		            else{
		                javaxt.io.File file = new javaxt.io.File(obj);
		                javaxt.io.File dest = new javaxt.io.File(destination + file.toString().substring(source.toString().length()));
		 
		                switch (eventID) {
		 
		                    case (Event.CREATE):js.autoSchedule();// file.copyTo(dest, true);
		                    
		                    System.out.println("createed else part");
		                    
		                    
		                    
		                    
		                    break;
		                    case (Event.MODIFY): file.copyTo(dest, true);
		                    
		                    File folder = new File("F:\\sqlfiles");
		                    File[] listOfFiles = folder.listFiles();
		                    String st = "successorfailure";
		               
		                    ModelAndView mdv = new ModelAndView("home");
		                    for (File file2 : listOfFiles) {
		                    	
		                    	if(file2.getName().equals(file.getName()))
		                    	{
		                    		 ScriptRunner scriptRunner = new ScriptRunner(js.getConnection());
		                 			
		                 			try {
		                 				Reader reader = new BufferedReader(new FileReader(file2));
		                 				  scriptRunner.runScript(reader);
		                 				  mdv.addObject(st, "Successfully executed for modified file");}
		                 				 catch (FileNotFoundException e) {
		                 					// TODO Auto-generated catch block
		                 					e.printStackTrace();
		                 				}
		                    	}
								
							}
		                    System.out.println("modified else part"); break;
		                    case (Event.RENAME): {
		                        javaxt.io.File orgFile = new javaxt.io.File(event.getOriginalFile());
		                        dest = new javaxt.io.File(destination + orgFile.toString().substring(source.toString().length()));
		                        dest.rename(file.getName());System.out.println("renamed else part");
		                        break;
		                    }
		                    
		                }
		 
		            }
		        }
		    }
		 
		    }
		    
		    
		    Connection getConnection()
		    {
		    	
				 try {
					 try {
						Class.forName("com.mysql.cj.jdbc.Driver");
						con=DriverManager.getConnection(  
								"jdbc:mysql://localhost:3306/dg","root","root");
					} catch (ClassNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					 
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return con;	
		    }
	@RequestMapping("/automode")
	public ModelAndView autoSchedule()
	{
		
		try {
			try {
				Class.forName("com.mysql.cj.jdbc.Driver");
				 con=DriverManager.getConnection(  
						"jdbc:mysql://localhost:3306/dg","root","root");
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}  
			
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}  

	      ModelAndView mdv = new ModelAndView("home");
		String st = "successorfailure";
		
		String dest ="F:\\sqlfilescopy";
		File folderCopy = new File("F:\\sqlfilescopy");
		Path pathCopy = folderCopy.toPath();
		File folder = new File("F:\\sqlfiles");
		Path path = folder.toPath();
		File[] fpc = folderCopy.listFiles();
      File[] listOfFiles = folder.listFiles();
      
      System.out.println("va value "+va +"fpc length"+fpc.length);
     /* if(va < listOfFiles.length)
      { 
    	  for(int i = va;i<listOfFiles.length;i++)
    	  {
    		  */
      if(fpc.length ==0)
      {
    	  for ( File file : listOfFiles) {
    		  
    		  ScriptRunner scriptRunner = new ScriptRunner(con);
			
			try {
				Reader reader = new BufferedReader(new FileReader(file));
				  scriptRunner.runScript(reader);
				  mdv.addObject(st, "Successfully executed");
				  try {
			    	  File newFile= new File(pathCopy+"\\"+file.getName());
			    	  if(newFile.createNewFile()) {
			    		  System.out.println("File created: " + newFile.getName());
			          } else {
			            System.out.println("File already exists.");
			          }
			    	
			    	  
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
				
		  }  
    	  Directory foldert = new Directory("F:\\sqlfiles");
  		Directory folderCopyt = new Directory("F:\\sqlfilescopy");
  		
  		try {
  			sync(foldert, folderCopyt);
  		} catch (Exception e) {
  			// TODO Auto-generated catch block
  			e.printStackTrace();
  		}
      }
      else
      {
    	   fpc = folderCopy.listFiles();
    	   System.out.println("fpc length else "+fpc.length);
    	  List<File> lisfile = new ArrayList<>();
    	  List<File> lisFileCopy = new ArrayList<>();
    	 
    	  
    	  for ( File file : listOfFiles)
    	  {
    		  lisfile.add(file);
    	  }
    	  for ( File filec : fpc)
    	  {
    		  lisFileCopy.add(filec);
    	  }
    	  System.out.println("lisfile size "+lisfile.size() + " lisfilecopy "+lisFileCopy.size());
    	 
    	  int x = lisfile.size();
    	  int y = lisFileCopy.size();
    	  int size =  x-y ;
    	  
    	  if(size !=0)
    	  {
    	  List<File> subLisFile = lisfile.subList((lisfile.size()-size), lisfile.size());
    	System.out.println("lis size is "+size);
    	  
    	  for (File file : subLisFile) {
			
    		  ScriptRunner scriptRunner = new ScriptRunner(con);
			
			try {
				Reader reader = new BufferedReader(new FileReader(file));
				  scriptRunner.runScript(reader);
				  mdv.addObject(st, "successfllyexecuted when adding new sql file to directory");
				  try {
			    	  File newFile= new File(pathCopy+"\\"+file.getName());
			    	  if(newFile.createNewFile()) {
			    		  System.out.println("File created: " + newFile.getName());
			          } else {
			            System.out.println("File already exists.");
			          }
			    	  
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
    	  
    	
    	  
      }else
      {
    	  mdv.addObject(st, "all scripts are executed successfully");
    	  System.out.println("Nothing to update");
      }
      
      /*for (File fileCop : fpc) {System.out.println("names in copy folder "+fileCop.getName());}
    		  
    		  for (File file : listOfFiles) {
				for(int i=0;i<listOfFiles.length;i++)
				{
					for(int j=i;j<fpc.length;)
					{
						
					}
				}
			
    		  for (File fileCop : fpc) {
    			  
    			  if(!(file.getName().equals(fileCop.getName())))
    			  {
    				  
					try {
						ScriptRunner scriptRunner = new ScriptRunner(datasource.getConnection());
						
						try {
							Reader reader = new BufferedReader(new FileReader(file));
							  scriptRunner.runScript(reader);
							  
						} catch (FileNotFoundException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
    		             
   		            
					} catch (SQLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
    					
    			  }else
    			  {
    				  System.out.println("All are already updated");
    			  }
				*/
			
    		  
			/*try {
				
				
				ScriptRunner scriptRunner = new ScriptRunner(datasource.getConnection());
				 Reader reader = new BufferedReader(new FileReader(listOfFiles[i]));
	             
	              scriptRunner.runScript(reader);
	              try {
	            	  File newFile= new File(pathCopy+"\\"+listOfFiles[i].getName());
	            	  if(newFile.createNewFile()) {
	            		  System.out.println("File created: " + newFile.getName());
	                  } else {
	                    System.out.println("File already exists.");
	                  }
	            	  
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	            // Files.cop
	             // FileUtils.copyFileUsingApache(listOfFiles[i], new File("apache.txt"));
	              mdv.addObject(st, "scripts are running successfully");
	              
	              
	              
			}catch (FileNotFoundException e) {
				
				e.printStackTrace();
				// TODO: handle exception
			}catch (RuntimeSqlException e) {
				// TODO Auto-generated catch block
				System.out.println("sql error");
				//e.printStackTrace();
			}catch (SQLException e) {
				// TODO: handle exception
				System.out.println("seconddd");
				
			}
			
			
			
			

              
    	    
    	  }*/
    	  
    		 // }
    		  //}
      }
      
      Directory foldert = new Directory("F:\\sqlfiles");
		Directory folderCopyt = new Directory("F:\\sqlfilescopy");
		
		try {
			sync(foldert, folderCopyt);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
			return mdv;

}
	
	/*
	 * public static void copyFile(String from, String to) throws IOException { Path
	 * src = Paths.get(from); Path dest = Paths.get(to); Files.copy(src.toFile(),
	 * dest.toFile()); }
	 */
	


	//Read more: https://www.java67.com/2016/09/how-to-copy-file-from-one-location-to-another-in-java.html#ixzz6aNsnQP8k
	
	private static void copy(File src, File dest) throws IOException {

		 InputStream is = null; OutputStream os = null;

		 try { is = new FileInputStream(src);
		 os = new FileOutputStream(dest); // buffer size 1K 
		byte[] buf = new byte[1024];
		 int bytesRead;
		 while ((bytesRead = is.read(buf)) > 0) 
		{ os.write(buf, 0, bytesRead); }
		 }
		 finally { is.close(); os.close(); }

		 }
		
		// get path object pointing to the directory we wish to monitor
	
		/*  File folder = new File("F:\\sqlfiles");
			Path path = folder.toPath();
          File[] listOfFiles = folder.listFiles();
		try {
			// get watch service which will monitor the directory
			WatchService watcher = path.getFileSystem().newWatchService();
			// associate watch service with the directory to listen to the event
			// types
			path.register(watcher, StandardWatchEventKinds.ENTRY_CREATE,StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.ENTRY_DELETE);
			System.out.println("Monitoring directory for changes...");
			// listen to events
			WatchKey watchKey = watcher.take();
			// get list of events as they occur
			List<WatchEvent<?>> events = watchKey.pollEvents();
			//iterate over events
			for (WatchEvent event : events) {
				//check if the event refers to a new file created
				if (event.kind() == StandardWatchEventKinds.ENTRY_CREATE) {
					 File[] listOfFiless = folder.listFiles();
					 if(listOfFiles.length <listOfFiless.length)
					 {
						 for(int i = (listOfFiles.length+1);i<=listOfFiless.length;i++)
						 {
							
							try {
								 ScriptRunner	scriptRunner = new ScriptRunner(datasource.getConnection());
								 Reader reader = new BufferedReader(new FileReader(listOfFiless[i-1]));
		     		               
		     		                scriptRunner.runScript(reader);
		     		                mdv.addObject(st, "scripts are running successfully");
							} catch (SQLException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}

     		                
     		               
						 }*/
						
						/*
						 * for (File file : listOfFiles) {
						 * 
						 * for (File fil : listOfFiless) {
						 * 
						 * if(file.getName() != fil.getName()) { ScriptRunner scriptRunner; try {
						 * scriptRunner = new ScriptRunner(datasource.getConnection()); Reader reader =
						 * new BufferedReader(new FileReader(file));
						 * 
						 * scriptRunner.runScript(reader); mdv.addObject(st,
						 * "scripts are running successfully");
						 * 
						 * 
						 * 
						 * } catch (SQLException e) { // TODO Auto-generated catch block
						 * e.printStackTrace(); }
						 * 
						 * 
						 * }
						 * 
						 * }
						 * 
						 * } //print file name which is newly created System.out.println("Created: " +
						 * event.kind().name());
						 */
				/*}else
				{
					System.out.println("already upto date");
				}
			}
		}
		}catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return mdv;
	   }*/
		
		 /* try {
		  
		  
		  File folder = new File("F:\\sqlfiles"); File[] listOfFiles =
		  folder.listFiles(); System.out.println("value of va before "+va);
		  Path path = folder.toPath();
		  BasicFileAttributes fatr = Files.readAttributes(path,
	                BasicFileAttributes.class);
		  
		  for (File file : listOfFiles) { 
			  
			  if(fatr.creationTime() !=  fatr.lastModifiedTime())
			  {
				  System.out.println("file "+fatr.creationTime()  +  fatr.lastModifiedTime()  );
			  ScriptRunner scriptRunner = new
		  ScriptRunner(datasource.getConnection());
		  
		  
		  Reader reader = new BufferedReader(new FileReader(file));
		  
		  scriptRunner.runScript(reader); mdv.addObject(st,
		  "scripts are running successfully");
		  
			  }
			  else
			  {
				  System.out.println("nothing to run");
			  }
		  
		  } } catch (Exception e) {
		  
		  mdv.addObject(st, "script running failed"); e.printStackTrace(); }
		 
		
		return mdv;
		
	}*/
	
	//selecting files and time from jsp
	@RequestMapping("/manualmodelist")
	public ModelAndView manualSchedule()
	{
		List<String> fileNames = new ArrayList<>();
		 File folder = new File("F:\\sqlfiles");
         File[] listOfFiles = folder.listFiles();
ModelAndView mdv = new ModelAndView("home");
         for (File file : listOfFiles) {
         
        	 fileNames.add(file.getName());
         }
		mdv.addObject("fName", fileNames);
		
		
		return mdv;
		
	}
	//after selecting files and manual time from jsp
	@RequestMapping("/manualmode")
	public ModelAndView manualmodeSch(HttpServletRequest request,HttpServletResponse response)
	{
		String dateTimeLocal = request.getParameter("datetimeloc");
		
		String[] fileNames = request.getParameterValues("fnames"); 
		
		System.out.println(dateTimeLocal +" "+ fileNames[0]);
		 File folder = new File("F:\\sqlfiles");
         File[] listOfFiles = folder.listFiles();
		ModelAndView mdb = new ModelAndView("home");
		TimerTask task = new TimerTask() {

			  public void run() {
				  
			    //do the task
				  try {
				  for (String fileName : fileNames) {
					
				  for (File file : listOfFiles) {
					  
					  if(fileName.equals(file.getName())) {
					  
					  ScriptRunner scriptRunner = new ScriptRunner(con);

		                
		                Reader reader = new BufferedReader(new FileReader(file));
		               
		                scriptRunner.runScript(reader);
					  }
				  }
				  }  
				 
				  
				  mdb.addObject("hello", "manual testing completed successfully");
				  }catch (Exception e) {
					System.out.println("some exception occured");
					mdb.addObject("hello", "manual testing failed");
					e.printStackTrace();
				}
			  }

			};
			
			
			try {
				Date futureDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(dateTimeLocal);
				System.out.println(futureDate);
				Timer timer = new Timer();
				timer.schedule(task, futureDate); 
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
					
		
		return new ModelAndView("home");
		
		
		
	}
	
	

}
