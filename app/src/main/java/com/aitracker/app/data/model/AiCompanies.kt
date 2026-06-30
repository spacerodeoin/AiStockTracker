package com.aitracker.app.data.model

/** Curated catalog of companies central to AI development, hardware, and infrastructure. */
object AiCompanies {

    val all: List<AiCompany> = listOf(
        AiCompany(
            symbol = "NVDA",
            name = "NVIDIA",
            category = AiCategory.SEMICONDUCTOR,
            hardwareFocus = "GPUs & the Blackwell/Hopper AI accelerator platform",
            description = "Dominant designer of data-center GPUs and the CUDA software stack that " +
                "powers the majority of large-scale AI training and inference.",
        ),
        AiCompany(
            symbol = "AMD",
            name = "Advanced Micro Devices",
            category = AiCategory.SEMICONDUCTOR,
            hardwareFocus = "Instinct MI300/MI350 accelerators & EPYC CPUs",
            description = "Primary GPU challenger to NVIDIA with the Instinct accelerator line and " +
                "the ROCm open software ecosystem.",
        ),
        AiCompany(
            symbol = "AVGO",
            name = "Broadcom",
            category = AiCategory.SEMICONDUCTOR,
            hardwareFocus = "Custom AI ASICs & high-speed AI networking",
            description = "Designs custom AI silicon for hyperscalers and supplies the networking " +
                "fabric that links AI clusters together.",
        ),
        AiCompany(
            symbol = "TSM",
            name = "Taiwan Semiconductor",
            category = AiCategory.SEMICONDUCTOR,
            hardwareFocus = "Leading-edge foundry & CoWoS advanced packaging",
            description = "The contract manufacturer that fabricates nearly all advanced AI chips, " +
                "including NVIDIA, AMD and Apple silicon.",
        ),
        AiCompany(
            symbol = "ASML",
            name = "ASML Holding",
            category = AiCategory.HARDWARE,
            hardwareFocus = "EUV lithography machines",
            description = "Sole supplier of extreme-ultraviolet lithography systems required to " +
                "manufacture the most advanced AI chips.",
        ),
        AiCompany(
            symbol = "MU",
            name = "Micron Technology",
            category = AiCategory.HARDWARE,
            hardwareFocus = "High-bandwidth memory (HBM) for accelerators",
            description = "Key supplier of HBM and DRAM that feeds data to AI accelerators at the " +
                "bandwidth modern models demand.",
        ),
        AiCompany(
            symbol = "ARM",
            name = "Arm Holdings",
            category = AiCategory.SEMICONDUCTOR,
            hardwareFocus = "CPU architecture IP for AI data centers & edge",
            description = "Licenses the CPU architecture increasingly used in AI servers and " +
                "on-device edge inference.",
        ),
        AiCompany(
            symbol = "INTC",
            name = "Intel",
            category = AiCategory.SEMICONDUCTOR,
            hardwareFocus = "Gaudi accelerators, CPUs & foundry ambitions",
            description = "Building AI accelerators (Gaudi) and a contract foundry business while " +
                "supplying server CPUs.",
        ),
        AiCompany(
            symbol = "SMCI",
            name = "Super Micro Computer",
            category = AiCategory.HARDWARE,
            hardwareFocus = "Liquid-cooled AI server & rack systems",
            description = "Builds high-density, liquid-cooled servers that integrate GPUs into " +
                "deployable AI rack systems.",
        ),
        AiCompany(
            symbol = "DELL",
            name = "Dell Technologies",
            category = AiCategory.HARDWARE,
            hardwareFocus = "Enterprise AI servers & infrastructure",
            description = "Major vendor of AI-optimized servers and storage for enterprise and " +
                "data-center deployments.",
        ),
        AiCompany(
            symbol = "MSFT",
            name = "Microsoft",
            category = AiCategory.CLOUD,
            hardwareFocus = "Azure AI supercomputers & Maia accelerators",
            description = "Runs Azure AI infrastructure, partners deeply with OpenAI, and develops " +
                "its own Maia AI silicon.",
        ),
        AiCompany(
            symbol = "GOOGL",
            name = "Alphabet (Google)",
            category = AiCategory.MODELS,
            hardwareFocus = "Tensor Processing Units (TPUs)",
            description = "Develops the Gemini model family and designs custom TPU accelerators for " +
                "training and serving them.",
        ),
        AiCompany(
            symbol = "AMZN",
            name = "Amazon",
            category = AiCategory.CLOUD,
            hardwareFocus = "Trainium & Inferentia custom AI chips",
            description = "AWS provides AI compute at scale and designs Trainium/Inferentia chips to " +
                "reduce reliance on merchant GPUs.",
        ),
        AiCompany(
            symbol = "META",
            name = "Meta Platforms",
            category = AiCategory.MODELS,
            hardwareFocus = "MTIA accelerators & massive GPU clusters",
            description = "Builds the open-weight Llama models and operates some of the largest GPU " +
                "training clusters in the world.",
        ),
        AiCompany(
            symbol = "PLTR",
            name = "Palantir",
            category = AiCategory.SOFTWARE,
            hardwareFocus = "AI orchestration on existing compute",
            description = "Delivers the AIP platform that operationalizes LLMs and AI models for " +
                "enterprise and government workflows.",
        ),
        AiCompany(
            symbol = "ORCL",
            name = "Oracle",
            category = AiCategory.CLOUD,
            hardwareFocus = "OCI GPU superclusters",
            description = "Oracle Cloud Infrastructure rents large GPU superclusters for AI training " +
                "to model developers.",
        ),
    )

    val symbols: List<String> = all.map { it.symbol }

    fun bySymbol(symbol: String): AiCompany? = all.firstOrNull { it.symbol == symbol }
}
