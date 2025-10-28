mod docx_glue;
use std::{fs::File, io::Read};
use rifgen::rifgen_attr::*;
use docx_rs::{AlignmentType, Docx, IndentLevel, NumberingId, Paragraph, Pic, Run, Table, TableCell, TableRow};

#[cfg(target_os = "android")]
use android_logger::Config;
#[cfg(target_os = "android")]
use log::Level as LogLevel;

pub use crate::docx_glue::*;

pub struct RustLog;

impl RustLog {
    #[generate_interface]
    pub fn initialise_logging() {
        #[cfg(target_os = "android")]
        android_logger::init_once(
            Config::default()
                .with_min_level(LogLevel::Trace)
                .with_tag("Rust"),
        );
        log_panics::init();
        log::info!("Logging initialised from Rust");
    }
}

#[generate_interface_doc]
pub struct AndroidDocBuilder {
    doc: Docx,
}

impl AndroidDocBuilder {
    #[generate_interface(constructor)]
    pub fn new() -> AndroidDocBuilder {
        AndroidDocBuilder { doc: Docx::new() }
    }

    #[generate_interface]
    pub fn add_text(&mut self, text: &str) -> bool {
        log::debug!("Adding Text {}", text);
        self.doc = std::mem::replace(&mut self.doc, Docx::new())
            .add_paragraph(Paragraph::new().add_run(Run::new().add_text(text)));
        true
    }

    #[generate_interface]
    pub fn add_formatted_text(&mut self, text: &str, bold: bool, italic: bool, _underline: bool, font_size: u32, color: &str) -> bool {
        log::debug!("Adding formatted text: {}", text);
        let mut run = Run::new().add_text(text);
        if bold { run = run.bold(); }
        if italic { run = run.italic(); }
        if font_size > 0 { run = run.size((font_size * 2) as usize); }
        if !color.is_empty() { run = run.color(color); }
        self.doc = std::mem::replace(&mut self.doc, Docx::new()).add_paragraph(Paragraph::new().add_run(run));
        true
    }

    #[generate_interface]
    pub fn add_paragraph_with_alignment(&mut self, text: &str, alignment: &str) -> bool {
        log::debug!("Adding paragraph with alignment: {}", alignment);
        let align = match alignment.to_lowercase().as_str() {
            "center" => AlignmentType::Center,
            "right" => AlignmentType::Right,
            "justify" => AlignmentType::Justified,
            _ => AlignmentType::Left,
        };
        self.doc = std::mem::replace(&mut self.doc, Docx::new())
            .add_paragraph(Paragraph::new().add_run(Run::new().add_text(text)).align(align));
        true
    }

    #[generate_interface]
    pub fn add_bullet_item(&mut self, text: &str) -> bool {
        log::debug!("Adding bullet item: {}", text);
        self.doc = std::mem::replace(&mut self.doc, Docx::new())
            .add_paragraph(Paragraph::new().add_run(Run::new().add_text(text))
                .numbering(NumberingId::new(1), IndentLevel::new(0)));
        true
    }

    #[generate_interface]
    pub fn add_numbered_item(&mut self, text: &str) -> bool {
        log::debug!("Adding numbered item: {}", text);
        self.doc = std::mem::replace(&mut self.doc, Docx::new())
            .add_paragraph(Paragraph::new().add_run(Run::new().add_text(text))
                .numbering(NumberingId::new(2), IndentLevel::new(0)));
        true
    }

    #[generate_interface]
    pub fn add_table(&mut self, rows: u32, cols: u32) -> bool {
        log::debug!("Adding table: {}x{}", rows, cols);
        let table_rows: Vec<_> = (0..rows).map(|_| {
            TableRow::new((0..cols).map(|_| TableCell::new().add_paragraph(Paragraph::new())).collect())
        }).collect();
        self.doc = std::mem::replace(&mut self.doc, Docx::new()).add_table(Table::new(table_rows));
        true
    }

    #[generate_interface]
    pub fn add_image(&mut self, file: &str, width: u32, height: u32) -> bool {
        log::debug!("Fetching file: {}", file);
        match File::open(file).and_then(|mut f| {
            let mut buf = Vec::new();
            f.read_to_end(&mut buf)?;
            Ok(if buf.len() > 500_000 { self.compress_image(&buf, width, height).unwrap_or(buf) } else { buf })
        }) {
            Ok(buf) => {
                self.doc = std::mem::replace(&mut self.doc, Docx::new())
                    .add_paragraph(Paragraph::new().add_run(Run::new().add_image(Pic::new(buf).size(width, height))));
                true
            }
            Err(e) => { log::error!("Error adding image: {}", e); false }
        }
    }

    fn compress_image(&self, buf: &[u8], max_width: u32, max_height: u32) -> Result<Vec<u8>, String> {
        use image::ImageFormat;
        use std::io::Cursor;
        let img = image::load_from_memory(buf).map_err(|e| e.to_string())?;
        let resized = if img.width() > max_width || img.height() > max_height {
            img.resize(max_width, max_height, image::imageops::FilterType::Lanczos3)
        } else { img };
        let mut output = Vec::new();
        resized.write_to(&mut Cursor::new(&mut output), ImageFormat::Jpeg).map_err(|e| e.to_string())?;
        log::debug!("Compressed image from {} to {} bytes", buf.len(), output.len());
        Ok(output)
    }

    #[generate_interface]
    pub fn generate_docx(&mut self, file_name: &str) -> bool {
        log::debug!("Exporting to {}", file_name);
        match std::fs::File::create(file_name) {
            Ok(f) => match self.doc.build().pack(f) {
                Ok(_) => true,
                Err(e) => { log::error!("Error packing docx: {:?}", e); false }
            },
            Err(e) => { log::error!("Error creating file: {}", e); false }
        }
    }
}
