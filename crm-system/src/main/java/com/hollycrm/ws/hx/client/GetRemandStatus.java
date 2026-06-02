package com.hollycrm.ws.hx.client;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>
 * getRemandStatus complex type�� Java �ࡣ
 *
 * <p>
 * ����ģʽƬ��ָ�������ڴ����е�Ԥ�����ݡ�
 *
 * <pre>
 * &lt;complexType name="getRemandStatus">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="msgHeader" type="{http://service.oppo.sdunicomsi.com/}msgHeader" minOccurs="0"/>
 *         &lt;element name="remandStatusSelectBean" type="{http://service.oppo.sdunicomsi.com/}remandStatusSelectBean" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "getRemandStatus", propOrder = { "msgHeader", "remandStatusSelectBean" })
public class GetRemandStatus {

	protected MsgHeader msgHeader;
	protected RemandStatusSelectBean remandStatusSelectBean;

	/**
	 * ��ȡmsgHeader���Ե�ֵ��
	 *
	 * @return possible object is {@link MsgHeader }
	 *
	 */
	public MsgHeader getMsgHeader() {
		return msgHeader;
	}

	/**
	 * ����msgHeader���Ե�ֵ��
	 *
	 * @param value
	 *          allowed object is {@link MsgHeader }
	 *
	 */
	public void setMsgHeader(MsgHeader value) {
		this.msgHeader = value;
	}

	/**
	 * ��ȡremandStatusSelectBean���Ե�ֵ��
	 *
	 * @return possible object is {@link RemandStatusSelectBean }
	 *
	 */
	public RemandStatusSelectBean getRemandStatusSelectBean() {
		return remandStatusSelectBean;
	}

	/**
	 * ����remandStatusSelectBean���Ե�ֵ��
	 *
	 * @param value
	 *          allowed object is {@link RemandStatusSelectBean }
	 *
	 */
	public void setRemandStatusSelectBean(RemandStatusSelectBean value) {
		this.remandStatusSelectBean = value;
	}

}
