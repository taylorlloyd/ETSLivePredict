
Proposal.pdf: Proposal.tex references.bib
	latexmk -pdf -halt-on-error Proposal.tex
	latexmk -c Proposal.tex
	rm Proposal.bbl

check:
	@echo "The following items may contain weak word usage.--------------------"
	@./checks/weasels.sh *.tex 1>&2
	@echo "The following items may contain passive language.-------------------"
	@./checks/passive_voice.sh *.tex 1>&2
	@echo "The following items may contain unnecessary duplication.------------"
	@perl ./checks/dups *.tex 2>&2
	@echo "Checking spelling.---------------------------------------------------"
	@ispell -p terms.txt -t *.tex
	@echo "Checking diction.---------------------------------------------------"
	@./checks/diction.sh *.tex 1>&2

style:
	@echo "Checking for nominalizations.---------------------------------------"
	@sh style.sh *.tex 1>&2

clean:
	rm -f *.out *.pdf *.aux *.dvi *.log *.blg *.bbl *.tex-e
