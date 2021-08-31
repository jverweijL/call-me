package call.me.portlet.action;

import call.me.constants.CallMePortletKeys;
import com.liferay.document.library.kernel.model.DLFileEntry;
import com.liferay.document.library.kernel.model.DLVersionNumberIncrease;
import com.liferay.document.library.kernel.service.DLAppLocalService;
import com.liferay.document.library.kernel.service.DLAppService;
import com.liferay.document.library.kernel.service.DLAppServiceUtil;
import com.liferay.document.library.kernel.service.DLFileEntryLocalService;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.model.Phone;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCActionCommand;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.ServiceContextFactory;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.kernel.workflow.WorkflowConstants;
import jdk.jfr.internal.StringPool;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Component(
        immediate = true,
        property = {
                "javax.portlet.name=" + CallMePortletKeys.CALLME,
                "mvc.command.name=/callme"
        },
        service = MVCActionCommand.class
)
public class CallMeMVCActionCommand implements MVCActionCommand {

    @Override
    public boolean processAction(ActionRequest actionRequest, ActionResponse actionResponse) throws PortletException {

        // must be made configurable..
        long repositoryID = 37544;
        long folderID = 40372;
        //long fileEntryTypeID = 0;
        String extension = ".pdf";
        String mimetype = "application/pdf";
        String phonenumber = null;

        ThemeDisplay themeDisplay = (ThemeDisplay) actionRequest.getAttribute(WebKeys.THEME_DISPLAY);

        // get primary phonenumber
        for(Phone phone:themeDisplay.getUser().getPhones()){
            if (phone.isPrimary()) {
                phonenumber = phone.getNumber();
            }
        }

        String filename = UUID.randomUUID().toString();

        Date tomorrow = new Date();
        Calendar c = Calendar.getInstance();
        c.setTime(tomorrow);
        c.add(Calendar.DATE, 1);
        tomorrow = c.getTime();

        // todo lookup name/phonenumber and store in file as json object
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PDDocument document = new PDDocument();
        PDPage page = new PDPage(PDRectangle.A4);

        try {
            document.addPage(page);
            PDPageContentStream contentStream = new PDPageContentStream(document, page);
            int textPosition = (int)(page.getBBox().getHeight() * (60f/100f));
            contentStream.setFont(PDType1Font.COURIER, 16);
            //contentStream.newLineAtOffset(15,25);
            contentStream.beginText();
            contentStream.newLineAtOffset(10, textPosition);
            contentStream.showText(String.format("Please call %s at %s",themeDisplay.getUser().getFullName(),phonenumber));
            contentStream.endText();
            contentStream.close();
            document.save(baos);
            document.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //String exampleString = "example";
        InputStream stream = new ByteArrayInputStream(baos.toByteArray()); //new ByteArrayInputStream(exampleString.getBytes(StandardCharsets.UTF_8));

        System.out.println("CallMe request send");

        System.out.println(themeDisplay);
        try {
            //https://liferay.dev/ask/questions/development/start-workflow-programmatically-after-uploaded-file-in-the-dl-1
            // create dummy file with expiration date should trigger workflow by definition in folder
            System.out.println("svc");
            ServiceContext serviceContext = ServiceContextFactory.getInstance(DLFileEntry.class.getName(), actionRequest);
            DLAppServiceUtil.addFileEntry(filename,repositoryID,folderID,filename+extension,mimetype,filename,"","",stream,baos.size(),tomorrow,tomorrow,serviceContext);

        } catch (PortalException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }

        System.out.println("HERE---------");

        return true;
    }

    @Reference
    private DLFileEntryLocalService _DLFileEntryLocalService;


}
