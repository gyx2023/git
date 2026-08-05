package com.crm.system.client.hx;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>
 * outParam complex type�� Java �ࡣ
 *
 * <p>
 * ����ģʽƬ��ָ�������ڴ����е�Ԥ�����ݡ�
 *
 * <pre>
 * &lt;complexType name="outParam">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="returnCode" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="returnMsg" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="remandStatusBean" type="{http://service.oppo.sdunicomsi.com/}remandStatusBean" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "outParam", propOrder = { "returnCode", "returnMsg", "remandStatusBean" })
public class OutParam {

	protected String returnCode;
	protected String returnMsg;
	protected List<RemandStatusBean> remandStatusBean;

	/**
	 * ��ȡreturnCode���Ե�ֵ��
	 *
	 * @return possible object is {@link String }
	 *
	 */
	public String getReturnCode() {
		return returnCode;
	}

	/**
	 * ����returnCode���Ե�ֵ��
	 *
	 * @param value
	 *          allowed object is {@link String }
	 *
	 */
	public void setReturnCode(String value) {
		this.returnCode = value;
	}

	/**
	 * ��ȡreturnMsg���Ե�ֵ��
	 *
	 * @return possible object is {@link String }
	 *
	 */
	public String getReturnMsg() {
		return returnMsg;
	}

	/**
	 * ����returnMsg���Ե�ֵ��
	 *
	 * @param value
	 *          allowed object is {@link String }
	 *
	 */
	public void setReturnMsg(String value) {
		this.returnMsg = value;
	}

	/**
	 * Gets the value of the remandStatusBean property.
	 *
	 * <p>
	 * This accessor method returns a reference to the live list, not a snapshot.
	 * Therefore any modification you make to the returned list will be present
	 * inside the JAXB object. This is why there is not a <CODE>set</CODE> method
	 * for the remandStatusBean property.
	 *
	 * <p>
	 * For example, to add a new item, do as follows:
	 *
	 * <pre>
	 * getRemandStatusBean().add(newItem);
	 * </pre>
	 *
	 *
	 * <p>
	 * Objects of the following type(s) are allowed in the list
	 * {@link RemandStatusBean }
	 *
	 *
	 */
	public List<RemandStatusBean> getRemandStatusBean() {
		if (remandStatusBean == null) {
			remandStatusBean = new ArrayList<RemandStatusBean>();
		}
		return this.remandStatusBean;
	}

}
