USE [ConsultorioDB]
GO
/****** Object:  Table [dbo].[Citas]    Script Date: 09/05/2025 01:58:07 p. m. ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[Citas](
	[id_cita] [int] IDENTITY(1,1) NOT NULL,
	[consultorio_id] [int] NOT NULL,
	[medico_id] [int] NOT NULL,
	[horario_consulta] [datetime] NOT NULL,
	[nombre_paciente] [varchar](100) NOT NULL,
PRIMARY KEY CLUSTERED 
(
	[id_cita] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[Consultorios]    Script Date: 09/05/2025 01:58:07 p. m. ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[Consultorios](
	[id_consultorio] [int] IDENTITY(1,1) NOT NULL,
	[numero_consultorio] [int] NOT NULL,
	[piso] [int] NOT NULL,
PRIMARY KEY CLUSTERED 
(
	[id_consultorio] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY],
UNIQUE NONCLUSTERED 
(
	[numero_consultorio] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[Doctores]    Script Date: 09/05/2025 01:58:07 p. m. ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[Doctores](
	[id_medico] [int] IDENTITY(1,1) NOT NULL,
	[nombre] [varchar](100) NOT NULL,
	[apellido_paterno] [varchar](100) NOT NULL,
	[apellido_materno] [varchar](100) NOT NULL,
	[especialidad] [varchar](100) NOT NULL,
PRIMARY KEY CLUSTERED 
(
	[id_medico] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
ALTER TABLE [dbo].[Citas]  WITH CHECK ADD  CONSTRAINT [FK_Cita_Consultorio] FOREIGN KEY([consultorio_id])
REFERENCES [dbo].[Consultorios] ([id_consultorio])
GO
ALTER TABLE [dbo].[Citas] CHECK CONSTRAINT [FK_Cita_Consultorio]
GO
ALTER TABLE [dbo].[Citas]  WITH CHECK ADD  CONSTRAINT [FK_Cita_Medico] FOREIGN KEY([medico_id])
REFERENCES [dbo].[Doctores] ([id_medico])
GO
ALTER TABLE [dbo].[Citas] CHECK CONSTRAINT [FK_Cita_Medico]
GO
