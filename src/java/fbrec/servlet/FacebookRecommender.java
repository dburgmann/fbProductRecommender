package fbrec.servlet;

import com.restfb.exception.FacebookGraphException;
import com.restfb.exception.FacebookNetworkException;
import com.restfb.exception.FacebookOAuthException;
import fbrec.error.Error;
import fbrec.error.PostProcessingException;
import fbrec.error.MatchingException;
import fbrec.error.TaggingException;
import fbrec.control.RecommendationProcess;
import fbrec.error.ConfigException;
import fbrec.ranking.Ranking;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * The servlet class which is responsible for handling of request and responses
 * initiates the recommendation process
 * @author Daniel
 */
public class FacebookRecommender extends HttpServlet {
    private String  accessToken;
    private int     numResults;
    private int     numTags;
    
    
    /**
     * Processes requests for both HTTP
     * <code>GET</code> and
     * <code>POST</code> methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException{
        
        //init vars
        RecommendationProcess process;
        Ranking result;
        PrintWriter out;
 
        //set content type
        response.setContentType("application/json;charset=UTF-8");
        out = response.getWriter();
        try{
            //get submitted parameters
            retrieveParameters(request);            
            
            //start process
            process = new RecommendationProcess(accessToken);
            result  = process.init(numResults, numTags).getRecommendations();

            //print result as json
            out.println(result.toJSONString());
        }catch(FacebookOAuthException fe){                                      //exception occured when authenticating to facebook
            out.write(Error.factory(fe).toJSONString());
        }catch(FacebookNetworkException fe){                                    //exception occured when connecting to facebook
            out.write(Error.factory(fe).toJSONString());
        }catch(FacebookGraphException fe){                                      //exception occured in the graph api of facebook
            out.write(Error.factory(fe).toJSONString());
        }catch(TaggingException me){                                             //exception occured in Module
            out.write(Error.factory(me).toJSONString());
        }catch(PostProcessingException fe){                                             //exception occured in Filter
            out.write(Error.factory(fe).toJSONString());
        }catch(MatchingException me){                                           //exception occured in Matcher
            out.write(Error.factory(me).toJSONString());
        }catch(ConfigException me){                                             //exception occured in Config
            out.write(Error.factory(me).toJSONString());
        }catch(Exception e){
            out.write(Error.factory(e).toJSONString());
        }
        out.close();
        
    }
    
    private void retrieveParameters(HttpServletRequest request){
        //accesstoken
        accessToken = request.getParameter("accessToken");                      
        
        //number of results
        if(request.getParameter("numResults") != null){                         
            try{
                int integer = Integer.parseInt(request.getParameter("numResults")); //check if submitted value is valid
                numResults = integer;
            }catch(NumberFormatException e){
                numResults = 0;
            }            
        }
        else{
            numResults = 0;
        }
        
        //number of tags
        if(request.getParameter("numTags") != null){                         
            try{
                int integer = Integer.parseInt(request.getParameter("numTags")); //check if submitted value is valid
                numTags = integer;
            }catch(NumberFormatException e){
                numTags = 0;
            }            
        }
        else{
            numTags = 0;
        }
    }
    
    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP
     * <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP
     * <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Facebook Recommender";
    }// </editor-fold>
}
