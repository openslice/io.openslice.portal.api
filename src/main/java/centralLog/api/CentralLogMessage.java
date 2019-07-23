/**
 * Copyright 2017 University of Patras 
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License.
 * You may obtain a copy of the License at:
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * 
 * See the License for the specific language governing permissions and limitations under the License.
 */

package centralLog.api;

public class CentralLogMessage {

	CLevel clevel;
	
	String message;

	/**
	 * 
	 */
	public CentralLogMessage() {

	}
		
	/**
	 * @param cLevel
	 * @param message
	 */
	public CentralLogMessage(CLevel cLevel, String message) {
		super();
		this.clevel = cLevel;
		this.message = message;
	}


	/**
	 * @return the cLevel
	 */
	public CLevel getclevel() {
		return clevel;
	}

	/**
	 * @param cLevel the cLevel to set
	 */
	public void setclevel(CLevel cLevel) {
		this.clevel = cLevel;
	}

	/**
	 * @return the message
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * @param message the message to set
	 */
	public void setMessage(String message) {
		this.message = message;
	}
	
	
}
