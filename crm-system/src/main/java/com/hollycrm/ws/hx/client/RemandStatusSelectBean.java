package com.hollycrm.ws.hx.client;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>
 * remandStatusSelectBean complex type�� Java �ࡣ
 *
 * <p>
 * ����ģʽƬ��ָ�������ڴ����е�Ԥ�����ݡ�
 *
 * <pre>
 * &lt;complexType name="remandStatusSelectBean">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="remandCodeList" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="remandCodeBean" type="{http://service.oppo.sdunicomsi.com/}remandCodeBean" maxOccurs="unbounded" minOccurs="0"/>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "remandStatusSelectBean", propOrder = { "remandCodeList" })
public class RemandStatusSelectBean {

	protected RemandStatusSelectBean.RemandCodeList remandCodeList;

	/**
	 * ��ȡremandCodeList���Ե�ֵ��
	 *
	 * @return possible object is {@link RemandStatusSelectBean.RemandCodeList }
	 *
	 */
	public RemandStatusSelectBean.RemandCodeList getRemandCodeList() {
		return remandCodeList;
	}

	/**
	 * ����remandCodeList���Ե�ֵ��
	 *
	 * @param value
	 *          allowed object is {@link RemandStatusSelectBean.RemandCodeList }
	 *
	 */
	public void setRemandCodeList(RemandStatusSelectBean.RemandCodeList value) {
		this.remandCodeList = value;
	}

	/**
	 * <p>
	 * anonymous complex type�� Java �ࡣ
	 *
	 * <p>
	 * ����ģʽƬ��ָ�������ڴ����е�Ԥ�����ݡ�
	 *
	 * <pre>
	 * &lt;complexType>
	 *   &lt;complexContent>
	 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
	 *       &lt;sequence>
	 *         &lt;element name="remandCodeBean" type="{http://service.oppo.sdunicomsi.com/}remandCodeBean" maxOccurs="unbounded" minOccurs="0"/>
	 *       &lt;/sequence>
	 *     &lt;/restriction>
	 *   &lt;/complexContent>
	 * &lt;/complexType>
	 * </pre>
	 *
	 *
	 */
	@XmlAccessorType(XmlAccessType.FIELD)
	@XmlType(name = "", propOrder = { "remandCodeBean" })
	public static class RemandCodeList {

		protected List<RemandCodeBean> remandCodeBean;

		/**
		 * Gets the value of the remandCodeBean property.
		 *
		 * <p>
		 * This accessor method returns a reference to the live list, not a
		 * snapshot. Therefore any modification you make to the returned list will
		 * be present inside the JAXB object. This is why there is not a
		 * <CODE>set</CODE> method for the remandCodeBean property.
		 *
		 * <p>
		 * For example, to add a new item, do as follows:
		 *
		 * <pre>
		 * getRemandCodeBean().add(newItem);
		 * </pre>
		 *
		 *
		 * <p>
		 * Objects of the following type(s) are allowed in the list
		 * {@link RemandCodeBean }
		 *
		 *
		 */
		public List<RemandCodeBean> getRemandCodeBean() {
			if (remandCodeBean == null) {
				remandCodeBean = new ArrayList<RemandCodeBean>();
			}
			return this.remandCodeBean;
		}

	}

}
