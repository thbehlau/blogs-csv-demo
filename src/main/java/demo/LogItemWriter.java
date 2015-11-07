package demo;

import java.util.List;

import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.transform.FieldSet;
import org.springframework.stereotype.Component;

@Component
public class LogItemWriter implements ItemWriter<FieldSet>{
	@Override
	public void write(List<? extends FieldSet> items) throws Exception {
		for (FieldSet fieldSet : items) {
			System.out.print("Fields " + fieldSet.getFieldCount() + ":");
			System.out.print(fieldSet.readString(0) + ",");
			System.out.print(fieldSet.readString(1) + ",");
			System.out.println(fieldSet.readString(2));
		}
	}

}
