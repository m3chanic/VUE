/*
 * -----------------------------------------------------------------------------
 *
 * <p><b>License and Copyright: </b>The contents of this file are subject to the
 * Mozilla Public License Version 1.1 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License
 * at <a href="http://www.mozilla.org/MPL">http://www.mozilla.org/MPL/.</a></p>
 *
 * <p>Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
 * the specific language governing rights and limitations under the License.</p>
 *
 * <p>The entire file consists of original code.  Copyright &copy; 2003, 2004
 * Tufts University. All rights reserved.</p>
 *
 * -----------------------------------------------------------------------------
 */
package edu.tufts.osidimpl.repository.sakai;

public class Utilities
{
	private static org.osid.id.IdManager idManager = null;
    private static org.osid.logging.WritableLog log = null;
	private static org.osid.OsidContext context = null;
	private static org.osid.authentication.AuthenticationManager authenticationManager = null;
	private static org.osid.shared.Type authenticationType = new Type("org.sakaiproject","authentication","sakai");
    private static org.osid.shared.Type collectionAssetType = new Type("org.sakaiproject","asset","siteCollection");
    private static org.osid.shared.Type resourceAssetType =  new Type("org.sakaiproject","asset","resource");
	private static java.util.Map sessionIdMap = new java.util.HashMap();
	private static org.osid.shared.Id repositoryId = null;
	private static String endpoint = null;
	private static String address = null;

	public static org.osid.shared.Type getCollectionAssetType()
	{
		return collectionAssetType;
	}
	
	public static org.osid.shared.Type getResourceAssetType()
	{
		return resourceAssetType;
	}
	
	public static void setOsidContext(org.osid.OsidContext c)
	{
		context = c;
	}
	
	public static org.osid.OsidContext getOsidContext()
	{
		return context;
	}
	
	public static void setAuthenticationManager(org.osid.authentication.AuthenticationManager a)
	{
		authenticationManager = a;
	}
	
	public static void setIdManager(org.osid.id.IdManager manager)
	{
		idManager = manager;
	}

	public static org.osid.id.IdManager getIdManager()
	{
		return idManager;
	}

	public static void setLog(org.osid.logging.WritableLog l)
	{
		log = l;
	}
	
	public static void log(String entry)
	{
		try {
			log.appendLog(entry);
		} catch (org.osid.logging.LoggingException lex) {
			// swallow exception since logging is a best attempt to log an exception anyway
		}   
	}

	public static void log(Throwable t)
	{
		try {
			//t.printStackTrace();
			log.appendLog(t.getMessage());
		} catch (org.osid.logging.LoggingException lex) {
			// swallow exception since logging is a best attempt to log an exception anyway
		}   
	}	
	
	public static String typeToString(org.osid.shared.Type type)
	{
		return type.getDomain() + "/" + type.getKeyword() + "@" + type.getAuthority();
	}
	
	public static void setRepositoryId(String idString)
	{
		try {
			repositoryId = idManager.getId(idString);
		} catch (Throwable t) {
		}
	}
	
	public static org.osid.shared.Id getRepositoryId()
	{
		if (repositoryId == null) {
			try {
				repositoryId = idManager.getId("E89F7F92-8C23-481B-AF8C-7AE169699F34-2595-000008D778AFEB53");				
			} catch (Throwable t) {
			}
		}
		return repositoryId;
	}
	
	public static void setSessionId(String sessionId, String key)
	{
		sessionIdMap.put(key,sessionId);
		//System.out.println("setting hive for key " + key);
	}
	
	public static String getSessionId(String key)
		throws org.osid.repository.RepositoryException
	{
		try {
			boolean isSessionIdStillActive = true;
			// actually test if it is active
			if (isSessionIdStillActive) {
				return (String)(sessionIdMap.get(key));
			} else {
				authenticationManager.authenticateUser(authenticationType);
				if (authenticationManager.isUserAuthenticated(authenticationType)) {
					String sessionId = (String)context.getContext("org.sakaiproject.sessionId." + key);
					if (sessionId == null) {
						throw new org.osid.repository.RepositoryException(org.osid.OsidException.CONFIGURATION_ERROR);
					}
					setSessionId(sessionId,key);
					return sessionId;
				} else {
					throw new org.osid.repository.RepositoryException(org.osid.OsidException.PERMISSION_DENIED);
				}
			}
		} catch (org.osid.authentication.AuthenticationException aex) {
			throw new org.osid.repository.RepositoryException(org.osid.OsidException.PERMISSION_DENIED);
		} catch (Throwable t) {
			throw new org.osid.repository.RepositoryException(t.getMessage());
		}
	}
	
	public static void setEndpoint(String ep)
	{
		endpoint = ep;
	}
	
	public static String getEndpoint()
	{
		return endpoint;
	}

	public static void setAddress(String a)
	{
		address = a;
	}
	
	public static String getAddress()
	{
		return address;
	}
	
	public static String expectedValue(org.w3c.dom.Element element, String tag)
		throws org.xml.sax.SAXParseException
	{
		String expected = null;
		org.w3c.dom.NodeList nameNodeList = element.getElementsByTagName(tag);
		int numNodes = nameNodeList.getLength();
		if (numNodes > 0) {
			org.w3c.dom.Element e = (org.w3c.dom.Element)nameNodeList.item(0);
			try {
				expected = e.getFirstChild().getNodeValue();
			} catch (java.lang.NullPointerException npe) {
			}
		}
		return expected;
	}	
}