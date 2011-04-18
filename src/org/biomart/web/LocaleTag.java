package org.biomart.web;

import java.io.IOException;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.Tag;
import org.biomart.api.rest.App;

/**
 *
 * @author jhsu
 */
public class LocaleTag  implements Tag {
	private PageContext pageContext = null;
	private Tag parent = null;

   @Override
	public int doStartTag() throws JspException {
        try {
            this.pageContext.getOut().write(GuiceServletConfig._locale.replaceFirst("_", "-"));
        } catch (IOException ioe) {
            throw new JspException("Error: IOException while writing to client");
        }
		return SKIP_BODY;
	}

   @Override
   public int doEndTag() throws JspException {
      return EVAL_PAGE;
   }

   @Override
   public void release() {
   }

   @Override
   public void setPageContext(PageContext pageContext) {
      this.pageContext = pageContext;
   }

   @Override
   public void setParent(Tag parent) {
      this.parent = parent;
   }

   @Override
   public Tag getParent() {
      return parent;
   }
}
