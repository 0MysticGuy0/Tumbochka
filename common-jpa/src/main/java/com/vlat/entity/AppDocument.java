package com.vlat.entity;

import lombok.*;

import javax.persistence.*;

@Getter
@Setter
@EqualsAndHashCode(exclude = "id")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "app_document")
public class AppDocument {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private Long id;
    @Column(name = "telegram_file_id")
    private String telegramFileId;
    @Column(name = "document_name")
    private String docName;
    @OneToOne
    @JoinColumn(name = "binary_content_id")
    private BinaryContent binaryContent;
    @Column(name = "mime_type")
    private String mimeType;
    @Column(name = "file_size")
    private Long fileSize;
}
