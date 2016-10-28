/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fbrec.util;

import fbrec.tagging.FbConnector.FbFriend;
import java.util.List;

import org.apache.lucene.analysis.util.FilteringTokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

/**
 *
 */
public class FriendsTokenFilter extends FilteringTokenFilter {
  private CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);
  private List<FbFriend> friends;
  
  /**
   *
   */
  public FriendsTokenFilter(TokenStream in, List<FbFriend> friends){
    super(true, in);
    this.friends = friends;
  }

  
  /**
   * Returns the next input Token whose term() is not a Friend.
   */
  @Override
  protected boolean accept() {
    for(FbFriend friend: friends){
        if(termAtt.toString().toLowerCase().equals(friend.firstName.toLowerCase())) return false;
    }
    return true;
  }

}
