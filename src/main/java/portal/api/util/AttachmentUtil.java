/*-
 * ========================LICENSE_START=================================
 * io.openslice.portal.api
 * %%
 * Copyright (C) 2019 openslice.io
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * =========================LICENSE_END==================================
 */

package portal.api.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opendaylight.yang.gen.v1.urn.etsi.osm.yang.vnfd.rev170228.vnfd.catalog.Vnfd;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

/**
 * @author ctranoris
 *
 */

public class AttachmentUtil {

	private static int BUFFER_SIZE = 4 * 1024;
	private String descriptorYAMLfile;
	private ByteArrayOutputStream iconfilePath;
	
	private static final transient Log logger = LogFactory.getLog(AttachmentUtil.class.getName());

	/**
	 * @param att
	 * @param filePath
	 * @return
	 */
	public static String saveFile(MultipartFile att, String filePath) {
		File file = new File(filePath + att.getOriginalFilename());
		try {
			att.transferTo(file);
			return file.getPath();
			
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
//		DataHandler handler = att.getDataHandler();
//		try {
//			InputStream stream = handler.getInputStream();
//			MultivaluedMap map = att.getHeaders();
//			File f = new File(filePath);
//			OutputStream out = new FileOutputStream(f);
//
//			int read = 0;
//			byte[] bytes = new byte[1024];
//			while ((read = stream.read(bytes)) != -1) {
//				out.write(bytes, 0, read);
//			}
//			stream.close();
//			out.flush();
//			out.close();
//			return f.getAbsolutePath();
//
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
		return null;
	}

	/**
	 * @param att
	 * @param filePath
	 * @return
	 * @throws IOException
	 */
	public static String saveFile(ByteArrayOutputStream att, String filePath) throws IOException {

		File f = new File(filePath);
		FileOutputStream fos;
		try {
			fos = new FileOutputStream(f);
			att.writeTo(fos);
			fos.close();
			return f.getAbsolutePath();
		} catch (IOException ioe) {
			// Handle exception here
			ioe.printStackTrace();
		} finally {
		}

		return null;

	}
	
    public static String extractYAMLfile(String filePath) throws IOException {
        
    	String descriptorYAMLfile = null;
    	try (InputStream in = new FileInputStream(filePath);
    		//unzip
            GzipCompressorInputStream gzipIn = new GzipCompressorInputStream(in);
    		//untar
            TarArchiveInputStream tarIn = new TarArchiveInputStream(gzipIn)){
            TarArchiveEntry entry = null;
            // Iterate through the files in the archive
            while ((entry = tarIn.getNextTarEntry()) != null) {              
                // If the file ends in .yaml
                if (entry.getName().endsWith(".yaml")) {

					logger.info("INFO: Examining " + entry.getName() + " for vnfd tag..." );
					// Create a new file
					ByteArrayOutputStream file = new ByteArrayOutputStream();
					
					int count;
					byte data[] = new byte[BUFFER_SIZE];
					// Read in chunks of BUFFER SIZE
					while((count = tarIn.read(data, 0, BUFFER_SIZE)) != -1) {
						// Write in a separate file.
					    file.write(data, 0, count);
					}
					// Set the file as the yaml file
					descriptorYAMLfile = new String(file.toByteArray());
						
			    }
			}
    	}
        return descriptorYAMLfile;
    }

    public static ByteArrayOutputStream extractIcon(String filePath) throws IOException {
    	ByteArrayOutputStream iconfilePath = null;
        try (InputStream in = new FileInputStream(filePath);
    		//unzip
            GzipCompressorInputStream gzipIn = new GzipCompressorInputStream(in);
    		//untar
            TarArchiveInputStream tarIn = new TarArchiveInputStream(gzipIn)){
            TarArchiveEntry entry = null;
            // Iterate through the files in the archive
            while ((entry = tarIn.getNextTarEntry()) != null) {              
                // If the file is a png or a jpg
                if  ( entry.getName().endsWith(".png") || entry.getName().endsWith(".jpg")) {                    	
					iconfilePath = new ByteArrayOutputStream();
					//Copy the file to iconfilePath
					int count;
					byte data[] = new byte[BUFFER_SIZE];
					while((count = tarIn.read(data, 0, BUFFER_SIZE)) != -1) {
						iconfilePath.write(data, 0, count);
					}                	
                }
            }
        }
        return iconfilePath;
    }
	
    public void extractYAMLfileAndIcon(String filePath) throws IOException {
    	try (InputStream in = new FileInputStream(filePath);
    		//unzip
            GzipCompressorInputStream gzipIn = new GzipCompressorInputStream(in);
    		//untar
            TarArchiveInputStream tarIn = new TarArchiveInputStream(gzipIn)){
            TarArchiveEntry entry = null;
            // Iterate through the files in the archive
            while ((entry = tarIn.getNextTarEntry()) != null) {              
                // If the file ends in .yaml
                if ( entry.getName().endsWith(".yaml") || entry.getName().endsWith(".yml")) {

					logger.info("INFO: Examining " + entry.getName() + " for vnfd tag..." );
					// Create a new file
					ByteArrayOutputStream file = new ByteArrayOutputStream();
					
					int count;
					byte data[] = new byte[BUFFER_SIZE];
					// Read in chunks of BUFFER SIZE
					while((count = tarIn.read(data, 0, BUFFER_SIZE)) != -1) {
						// Write in a separate file.
					    file.write(data, 0, count);
					}
					// Set the file as the yaml file
					this.setDescriptorYAMLfile(new String(file.toByteArray()));					
			    }
                // If the file is a png or a jpg
                if  ( entry.getName().endsWith(".png") || entry.getName().endsWith(".jpg")) {                    	
					this.iconfilePath = new ByteArrayOutputStream();
					//Copy the file to iconfilePath
					int count;
					byte data[] = new byte[BUFFER_SIZE];
					while((count = tarIn.read(data, 0, BUFFER_SIZE)) != -1) {
						this.iconfilePath.write(data, 0, count);
					}                	
                }                
			}
    	}
    }

	public String getDescriptorYAMLfile() {
		return descriptorYAMLfile;
	}

	public void setDescriptorYAMLfile(String descriptorYAMLfile) {
		this.descriptorYAMLfile = descriptorYAMLfile;
	}

	public ByteArrayOutputStream getIconfilePath() {
		return iconfilePath;
	}

	public void setIconfilePath(ByteArrayOutputStream iconfilePath) {
		this.iconfilePath = iconfilePath;
	}
	
}
