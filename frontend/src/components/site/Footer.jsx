import { Coffee, Mail } from "lucide-react"

const contacts = [
    {
        label: "rafaelszccontato@gmail.com",
        link: "mailto:rafaelszccontato@gmail.com",
        icon: <Mail className="text-secondary h-5 w-5" />
    },
    {
        label: "github.com/rafaelszc",
        link: "https://github.com/rafaelszc",
        icon: <img src="src/assets/github.png" alt="github-logo" className="h-5 w-5" />
    },
    {
        label: "linkedin.com/in/rafaelszc",
        link: "https://linkedin.com/in/rafaelszc",
        icon: <img src="src/assets/linkedin.png" alt="linkedin-logo" className="h-5 w-5" />
    }
]

export const Footer = () => {
  return (
    <footer className="bg-primary w-full">
        <div className="p-20 flex items-center justify-between">
            <div className="flex gap-4 flex-col">
                <div className="flex gap-3 items-center">
                    <Coffee className="text-secondary"/>
                    <h1 className="text-2xl">CoffeeBabe</h1>
                </div>
                <div className="w-1/2">
                    <span className="text-sm text-secondary font-light tracking-wide">Artisanal coffee, signature desserts, and cozy moments. From our roastery to your day.</span>
                </div>
            </div>
            <div className="flex flex-col gap-3">
                <h2 className="text-xl">Contacts</h2>
                {contacts.map((contact) => (
                    <a
                        key={contact.link}
                        href={contact.link}
                        target="_blank"
                        rel="noopener noreferrer"
                        className="flex items-center gap-2 text-secondary text-xs font-light hover:text-secondary/80 transition-colors duration-300"
                    >
                        {contact.icon}
                        <span>{contact.label}</span>
                    </a>
                ))}
            </div>
        </div>
        <div className="border-t p-5 w-full text-center">
            <span className="font-light text-sm text-secondary block">&copy; 2023 CoffeeBabe. All rights reserved.</span>
            <span className="font-light text-sm text-secondary block">Rafael Souza. MIT License</span>
        </div>
    </footer>
  )
}